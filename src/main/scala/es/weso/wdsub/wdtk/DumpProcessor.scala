package es.weso.wdsub.wdtk

import org.wikidata.wdtk.dumpfiles._
import cats.effect._
import cats.implicits._
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path
import es.weso.shex
import java.io.OutputStream
import org.apache.commons.compress.compressors.gzip._
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.io.BufferedOutputStream
import org.wikidata.wdtk.datamodel.implementation.SitesImpl
import org.wikidata.wdtk.rdf.PropertyRegister
import org.eclipse.rdf4j.rio.RDFFormat
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentDumpProcessor
import es.weso.utils.VerboseLevel
import es.weso.wshex._
import es.weso.wdsub.DumpOptions
import es.weso.wdsub.writer._
import es.weso.wdsub._

/**
  * Dump processor using Wikidata toolkit DumpProcessingController
 **/
object DumpProcessor {

  private lazy val dumpProcessingController: DumpProcessingController = new DumpProcessingController("wdsubDump")
  private lazy val logger                                             = LoggerFactory.getLogger(this.getClass().getCanonicalName());

  private def info(msg: String): IO[Unit] = IO {
    logger.info(msg)
  }

  private def acquireShExProcessor(
      wshex: WSchema,
      outputPath: Option[Path],
      opts: DumpOptions
  ): IO[ShExProcessor] =
    for {
      _ <- if (opts.showSchema) {
        IO.println(s"Schema: $wshex")
      } else IO.unit
      maybeOut <- acquireOutput(outputPath)
    } yield {
      val shexProcessor = WDTKProcessor(wshex, maybeOut.map(DumpWriter.fromOutputStream(_, opts.dumpFormat)), opts)
      shexProcessor.open()
      ShExProcessor(shexProcessor, shexProcessor)
    }

  private def mkShExProcessor(
      schema: WSchema,
      outputPath: Option[Path],
      opts: DumpOptions
  ): Resource[IO, ShExProcessor] =
    Resource.make(acquireShExProcessor(schema, outputPath, opts))(
      shExProcessor =>
        IO {
          if (opts.verbose) println(s"End of process...")
          shExProcessor.entityDocumentDumpProcessor.close()
        }
    )

  private def acquireMwLocalDumpFile(file: Path, verbose: Boolean): IO[MwLocalDumpFile] = {
    val fileName = file.toFile().getAbsolutePath()
    info(s"File: $fileName") *> IO { new MwLocalDumpFile(fileName) }
  }

  private def mkDumpFile(file: Path, verbose: Boolean): Resource[IO, MwLocalDumpFile] =
    Resource.make(acquireMwLocalDumpFile(file, verbose))(
      mwFile => IO { mwFile.getDumpFileStream().close() }
    )

  // TODO: Check if we can run this in a different thread?
  // Some snippets have been taken from:
  // https://github.com/bennofs/wdumper/blob/master/src/main/java/io/github/bennofs/wdumper/DumpRunner.java#L97
  private def acquireOutput(maybeOutputPath: Option[Path]): IO[Option[OutputStream]] = maybeOutputPath match {
    case Some(outputPath) =>
      IO {
        val fileStream: OutputStream =
          Files.newOutputStream(
            outputPath,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
          )
        val bufferedStream: OutputStream = new BufferedOutputStream(fileStream, 10 * 1024 * 1024)
        val gzipParams                   = new GzipParameters()
        gzipParams.setCompressionLevel(1)
        val outputStream: OutputStream = new GzipCompressorOutputStream(bufferedStream, gzipParams)
        Some(outputStream)
      }
    case None => none[OutputStream].pure[IO]
  }

  private def releaseOutput(maybeOut: Option[OutputStream]): IO[Unit] =
    maybeOut match {
      case Some(out) => IO { out.close() }
      case None      => ().pure[IO]
    }
  private def mkOutput(maybeOutput: Option[Path]): Resource[IO, Option[OutputStream]] =
    Resource.make(acquireOutput(maybeOutput))(releaseOutput)

  /**
    * Dump process
    *
    * @param fileName name of filename to process
    * @param schema ShEx schema
    * @param verbose if true, show info messages
    * @param timeout timeout in seconds or 0 if no timeout should be used
    * @param outputFormat output format
    * @return dump results
    */
  def dumpProcess(
      filePath: Path,
      outPath: Option[Path],
      schema: WSchema,
      opts: DumpOptions
  ): IO[DumpResults] = {
    for {
      results <- (
        mkShExProcessor(schema, outPath, opts),
        mkDumpFile(filePath, opts.verbose)
      ).tupled
        .use {
          case (processor, mwDumpFile) =>
            for {
              _ <- IO { LogConfig.configureLogging() }
              _ <- IO {
                dumpProcessingController.registerEntityDocumentProcessor(
                  processor.entityDocumentDumpProcessor,
                  null,
                  true
                )
              }
              _ <- info(s"Processing local dump: $filePath")
              _ <- info(s"DateStamp: ${mwDumpFile.getDateStamp()}")
              _ <- info(s"Available?: ${mwDumpFile.isAvailable()}")
              _ <- IO { dumpProcessingController.processDump(mwDumpFile) }
            } yield DumpResults(
              processor.entityCounter.getTotalEntities(),
              processor.entityCounter.getMatchedEntities()
            )
        }
    } yield results
  }
}
