package es.weso.wdsub
import cats.implicits._
import fs2._
import fs2.{Stream, text}
import fs2.io._
import fs2.compression._
import java.nio.file.Path
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import cats.effect._
import java.io.InputStream
import java.io.OutputStream
import fs2.io.file.Files


sealed abstract class ParsedLine
case object OpenBracket extends ParsedLine
case object CloseBracket extends ParsedLine
case class ParsedItem(item: Item) extends ParsedLine
case class Error(str: String) extends ParsedLine

// Attempt to create a dump processor based on fs2
object IODumpProcessor {

  val ChunkSize: Int = 4096

  /**
    * Process all items in a file applying to each item the function `withItem` and storing the contents in `os` 
    *
    * @param path
    * @param os
    * @param withItem
    * @param opts
    * @return
    */
  def process(
    is: InputStream, 
    os: OutputStream, 
    withItem: ItemDocument => Option[String], 
    opts: DumpOptions = DumpOptions.default
    ): IO[Unit] = {
    val x = 
      readInputStream(is.pure[IO], opts.chunkSize)
      .through(when(opts.decompressInput, decompress))
      .through(text.utf8Decode)
      .through(text.lines)
      .through(parseInput)
      .through(processLines(withItem))
      .intersperse("\n")
      .through(text.utf8Encode)
      .through(when(opts.compressOutput, compress))
      .through(writeOutputStream(os.pure[IO]))
    x.compile.drain
  }

  private def decompress: Pipe[IO, Byte, Byte] = s =>
    s.through(Compression[IO].gunzip()).flatMap(_.content)

  private def compress: Pipe[IO, Byte, Byte] = s =>
    s.through(Compression[IO].gzip())

  private def when[A](cond: Boolean, action: Stream[IO,A] => Stream[IO,A]): Pipe[IO, A, A] = s =>
     if (cond) s.through(action)
     else s 

  private def processLines(withItem: ItemDocument => Option[String])(lines: Stream[IO, ParsedLine]): Stream[IO,String] = lines.map(_ match {
    case CloseBracket => "]"
    case OpenBracket => "["
    case ParsedItem(item) => withItem(item.itemDocument) match {
      case None => ""
      case Some(str) => str
    }
  })

  private def parseInput(s: Stream[IO, String]): Stream[IO,ParsedLine] = s.map(str => str match {
    case "[" => OpenBracket
    case "]" => CloseBracket
    case str   => parseItemDocument(str) match {
      case Left(str) => Error(str)
      case Right(item) => ParsedItem(item)
    }
  })

  private def parseItemDocument(str: String): Either[String, Item] = 
    Item.fromJsonStr(str)


}


