package es.weso.wdsub
import cats.implicits._
import fs2._
import fs2.{Stream, text}
import fs2.io._
import fs2.compression._
import java.nio.file.Path
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import cats.effect._
import java.io._
import fs2.io.file.Files
import java.nio.file.{Files => JavaFiles, Paths}
import es.weso.shex
import java.nio.file.StandardOpenOption._
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import es.weso.wshex._

case class DumpProcessorError(msg: String) extends RuntimeException(msg)

sealed abstract class ParsedLine
case object OpenBracket extends ParsedLine
case object CloseBracket extends ParsedLine
case class ParsedEntity(entity: Entity) extends ParsedLine
case class Error(str: String) extends ParsedLine
case object EndStream extends ParsedLine

/**
 * Dump processor based on fs2
 **/
object IODumpProcessor {

//  val ChunkSize: Int = 4096
  private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

  /**
    * Process all entities in a file applying to each entity the function `withEntity` and storing the contents in `os` 
    *
    * @param path
    * @param os
    * @param withEntity
    * @param opts
    * @return an IO action
    */
  def process(
    is: InputStream, 
    os: Option[OutputStream], 
    withEntity: Entity => IO[Option[String]], 
    refResults: Ref[IO,DumpResults],
    opts: DumpOptions = DumpOptions.default,
    ): IO[DumpResults] = {
    val x = 
      readInputStream(is.pure[IO], opts.chunkSize)
      .through(when(opts.decompressInput, decompress))
      .through(text.utf8Decode)
      .through(text.lines)
      .parEvalMap(opts.maxConcurrent)(processLine(withEntity, opts))
      .through(text.utf8Encode)
      .through(when(opts.compressOutput && os.isDefined, compress))
      .through(when(os.isDefined,writeOutputStream(os.get.pure[IO])))
    for { 
      _ <- x.compile.drain
      results <- refResults.get
    } yield results  
  }

  def processDump(
    is: InputStream, 
    os: OutputStream, 
    withEntity: Entity => IO[Option[String]], 
    opts: DumpOptions = DumpOptions.default
    ): IO[Unit] = {
      readInputStream(is.pure[IO], opts.chunkSize)
      .through(Compression[IO].gunzip())
      .flatMap(_.content)
      .through(text.utf8Decode)
      .through(text.lines)
      .parEvalMap(opts.maxConcurrent)(processLine(withEntity, opts))
      .through(text.utf8Encode)
      .through(Compression[IO].gzip())
      .through(writeOutputStream(os.pure[IO]))
      .compile
      .drain
  }

  def processLine(withEntity: Entity => IO[Option[String]], opts: DumpOptions)(line: String): IO[String] = for {
    parsedLine <- parseLine(line, opts)
    result <- processParsedLine(withEntity, parsedLine)
  } yield result

/*  def ioDumpProcess(filePath: Path, outPath: Path, schemaPath: Path, verbose: Boolean, timeout: Int): IO[DumpResults] = {
       for {
         is <- IO { JavaFiles.newInputStream(filePath) }
         os <- IO { JavaFiles.newOutputStream(outPath, CREATE) }
         /* schema <- shex.Schema.fromFile(schemaPath.toFile().getAbsolutePath())
         resolvedSchema <- shex.ResolvedSchema.resolve(schema, None)
         wshex <- IO.fromEither(ShEx2WShEx.convertSchema(resolvedSchema)) */
         wshex <- WShEx.fromPath(filePath)
         matcher = new Matcher(wShEx = wshex, verbose = verbose)
         _ <- process(is, os, checkSchema(matcher))
       } yield DumpResults(0,0)
  }*/


  private def decompress: Pipe[IO, Byte, Byte] = s =>
    s.through(Compression[IO].gunzip()).flatMap(_.content)

  private def compress: Pipe[IO, Byte, Byte] = s =>
    s.through(Compression[IO].gzip())

  private def when[A](cond: Boolean, action: => Stream[IO,A] => Stream[IO,A]): Pipe[IO, A, A] = s =>
     if (cond) s.through(action)
     else s 

  private def processParsedLine(
    withEntity: Entity => IO[Option[String]],
    parsedLine: ParsedLine
  ):IO[String] = parsedLine match {
      case OpenBracket => "[\n".pure[IO]
      case CloseBracket => "]\n".pure[IO]
      case ParsedEntity(e) => withEntity(e).map(_.map(_ + ",\n")).map(_.getOrElse(""))
      case Error(e) => "".pure[IO]
      case EndStream => "".pure[IO]
  }
    
  private def parseLine(line: String, opts: DumpOptions): IO[ParsedLine] = (line match {
    case "[" => OpenBracket.pure[IO]
    case "]" => CloseBracket.pure[IO]
    case str   => Entity.fromJsonStr(str, opts.jsonDeserializer).map(ParsedEntity)
  }).handleErrorWith(e => e match {
    case e:MismatchedInputException => EndStream.pure[IO]
    case _ => Error(e.getMessage()).pure[IO]
  })

}


