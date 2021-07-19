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
import java.nio.file.{Files => JavaFiles, Paths}
import es.weso.shex
import java.nio.file.StandardOpenOption._;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument

case class DumpProcessorError(msg: String) extends RuntimeException(msg)

sealed abstract class ParsedLine
case object OpenBracket extends ParsedLine
case object CloseBracket extends ParsedLine
case class ParsedEntity(entity: Entity) extends ParsedLine
case class Error(str: String) extends ParsedLine
case object EndStream extends ParsedLine

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
    withEntity: Entity => IO[Option[String]], 
    opts: DumpOptions = DumpOptions.default
    ): IO[Unit] = {
    val x = 
      readInputStream(is.pure[IO], opts.chunkSize)
      .through(when(opts.decompressInput, decompress))
      .through(text.utf8Decode)
      .through(text.lines)
      .through(parseInput)
      .through(processLines(withEntity))
//      .intersperse("\n")
      .through(text.utf8Encode)
      .through(when(opts.compressOutput, compress))
      .through(writeOutputStream(os.pure[IO]))
    x.compile.drain
  }

  def ioDumpProcess(filePath: Path, outPath: Path, schemaPath: Path, verbose: Boolean, timeout: Int): IO[DumpResults] = {
       for {
         is <- IO { JavaFiles.newInputStream(filePath) }
         os <- IO { JavaFiles.newOutputStream(outPath, CREATE) }
         schema <- shex.Schema.fromFile(schemaPath.toFile().getAbsolutePath())
         resolvedSchema <- shex.ResolvedSchema.resolve(schema, None)
         wshex <- IO.fromEither(ShEx2WShEx.convertSchema(resolvedSchema))
         matcher = new Matcher(wshex, verbose)
         _ <- process(is, os, checkSchema(matcher))
       } yield DumpResults(0,0)
  }

  private def checkSchema(matcher: Matcher)(entity: Entity): IO[Option[String]] = {
    entity.entityDocument match {
      case id: ItemDocument => {
        if (matcher.matchSomeShape(id).size > 0) {
          Some(Item(id).asJsonStr()).pure[IO]
        }
        else none.pure[IO]
      }
      case pd: PropertyDocument => none.pure[IO] // TODO. Check if it belongs to schema properties
      case _ => none.pure[IO]
    }
  }

     


  private def decompress: Pipe[IO, Byte, Byte] = s =>
    s.through(Compression[IO].gunzip()).flatMap(_.content)

  private def compress: Pipe[IO, Byte, Byte] = s =>
    s.through(Compression[IO].gzip())

  private def when[A](cond: Boolean, action: Stream[IO,A] => Stream[IO,A]): Pipe[IO, A, A] = s =>
     if (cond) s.through(action)
     else s 

  private def processLines(
    withEntity: Entity => IO[Option[String]]
  ): Pipe[IO, ParsedLine, String] = s => for {
    line <- s
    str <- line match {
      case OpenBracket => Stream.emit("[\n")
      case CloseBracket => Stream.emit("]\n")
      case ParsedEntity(e) => Stream.eval(withEntity(e).map(_.map(_ + ",\n")).map(_.getOrElse("")))
      case EndStream => Stream.emit("\n")
      case Error(msg) => Stream.raiseError[IO](DumpProcessorError(msg))
    }
  } yield str
    
  private def parseInput(s: Stream[IO, String]): Stream[IO,ParsedLine] = s.map(str => str match {
    case "[" => OpenBracket
    case "]" => CloseBracket
    case str   => Entity.fromJsonStr(str) match {
      case Left(End) => EndStream
      case Left(ParserError(exc)) => Error(s"Error parsing input: ${exc.getMessage()}\nStr: ${str}")
      case Right(e) => ParsedEntity(e)
    }
  })



}


