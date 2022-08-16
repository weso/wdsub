package es.weso.wdsubmain
import buildinfo._
import cats.data.Validated
import cats.effect._
import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import es.weso.utils.decline._
import es.weso.utils.named._
import es.weso.wshex._
import es.weso.wshex.matcher._
import es.weso.wdsub._
import es.weso.wbmodel._
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument

import java.io._
import java.nio.file.StandardOpenOption._
import java.nio.file.{Path, Files => JavaFiles}
import es.weso.utils.VerboseLevel
import es.weso.wshex.WShExFormat._
import es.weso.wikibasedel.EntityFetcher
import es.weso.wdsub.fs2processor._
import es.weso.wdsub.wdtk.DumpProcessor
import es.weso.utils.VerboseLevel._

sealed trait Processor {
  val name: String
}
object Processor {
  case object WDTK extends Processor { override val name = "WDTK" }
  case object Fs2  extends Processor { override val name = "Fs2"  }
}

sealed abstract class DumpActionOpt extends Named {
  def toDumpAction: IO[DumpAction]
}
object DumpActionOpt {
  case class FilterBySchemaOpt(path: Path, format: WShExFormat, verbosity: VerboseLevel) extends DumpActionOpt {
    val name                         = "Filter"
    def toDumpAction: IO[DumpAction] = DumpAction.filterBySchema(path, format, verbosity)
  }
  case object CountEntitiesOpt extends DumpActionOpt {
    val name                         = "countEntities"
    def toDumpAction: IO[DumpAction] = DumpAction.CountEntities.pure[IO]
  }
  case class ShowEntitiesOpt(maxEntities: Option[Int]) extends DumpActionOpt {
    val name                         = "showEntities"
    def toDumpAction: IO[DumpAction] = DumpAction.ShowEntities(maxEntities).pure[IO]
  }
}

case class Dump(
    filePath: Path,
    action: DumpActionOpt,
    outPath: Option[Path],
    opts: DumpOptions,
    processor: Processor,
    outputFormat: OutputFormat
)

case class ProcessEntity(entity: String)

object Main
    extends CommandIOApp(
      name = "wdsub",
      header = "Wikidata subsetting command line tool",
      version = BuildInfo.version
    ) {

  lazy val DUMP_FILE = "modules/wdsub/src/resources/sample-dump-20150815.json.gz"

  val processEntity: Opts[ProcessEntity] =
    Opts.subcommand("extract", "Show information about an entity.") {
      Opts.option[String]("entity", "Entity name", short = "e").map(ProcessEntity)
    }

  private lazy val filePath         = Opts.argument[Path](metavar = "dumpFile")
  private lazy val processors       = List(Processor.WDTK, Processor.Fs2)
  private lazy val processorNames   = processors.map(_.name)
  private lazy val defaultProcessor = processors.head

  private lazy val outputFormats       = List(OutputFormat.JsonDump, OutputFormat.TurtleDump)
  private lazy val outputFormatNames   = outputFormats.map(_.name)
  private lazy val defaultOutputFormat = outputFormats.head

  private lazy val schemaFormats       = List(ESCompactFormat, CompactWShExFormat)
  private lazy val schemaFormatNames   = schemaFormats.map(_.name)
  private lazy val defaultSchemaFormat = schemaFormats.head

  private lazy val dumpModes       = DumpMode.availableModes
  private lazy val dumpModesNames  = dumpModes.map(_.name)
  private lazy val defaultDumpMode = dumpModes.head

  private val maxStatements = Opts.option[Int]("maxStatements", "max statements to show").orNone

  private val processor =
    Opts
      .option[String]("processor", help = s"Dump processor library. Possible values: ${processorNames.mkString(",")}")
      .mapValidated(
        str =>
          processors.find(_.name.toLowerCase == str.toLowerCase()) match {
            case None =>
              Validated
                .invalidNel(s"Invalid processor name: $str. Available processors: ${processorNames.mkString(",")}")
            case Some(p) => Validated.valid(p)
          }
      )
      .withDefault(defaultProcessor)

  private val schemaFormat =
    Opts
      .option[String]("schemaFormat", help = s"Schema format: ${schemaFormatNames.mkString(",")}")
      .mapValidated(
        str =>
          schemaFormats.find(_.name == str) match {
            case None =>
              Validated
                .invalidNel(
                  s"Invalid processor name: $str. Available schema formats: ${schemaFormatNames.mkString(",")}"
                )
            case Some(p) => Validated.valid(p)
          }
      )
      .withDefault(defaultSchemaFormat)

  private val outputFormat =
    Opts
      .option[String]("outputFormat", help = s"Output format. Possible values: ${outputFormatNames.mkString(",")}")
      .mapValidated(
        str =>
          outputFormats.find(_.name == str) match {
            case None =>
              Validated
                .invalidNel(s"Invalid output format: $str. Available processors: ${outputFormatNames.mkString(",")}")
            case Some(p) => Validated.valid(p)
          }
      )
      .withDefault(defaultOutputFormat)

  private val schemaPath = Opts.option[Path]("schema", help = "ShEx schema", short = "s", metavar = "file")

  private val outPath = Opts.option[Path]("out", help = "output path", short = "o", metavar = "file").orNone

  private val verbose     = Opts.flag("verbose", "Verbose mode").orFalse
  private val showCounter = Opts.flag("showCounter", "Show counter at the end of process").orTrue
  private val showSchema  = Opts.flag("showSchema", "Show schema").orFalse

//  private val compressOutput = Opts.flag("compressOutput", "compress output").orTrue
  lazy val booleans = List("true", "false")
  private val compressOutput =
    Opts
      .option[String]("compressOutput", help = s"Compress output. Possible values: ${booleans.mkString(",")}")
      .mapValidated(
        str =>
          booleans.find(_ == str.toLowerCase()) match {
            case None =>
              Validated
                .invalidNel(s"Invalid compressOutput value: $str. Available values: ${booleans.mkString(",")}")
            case Some("true")  => Validated.valid(true)
            case Some("false") => Validated.valid(false)
            case x =>
              Validated
                .invalidNel(s"Invalid compressOutput value: $x. Available values: ${booleans.mkString(",")}")

          }
      )
      .withDefault(true)

  private val dumpMode =
    validatedList("dumpMode", DumpMode.availableModes)

  private val dumpOpts: Opts[DumpOptions] = (verbose, showCounter, compressOutput, showSchema, dumpMode).mapN {
    case (v, sc, co, ss, dm) =>
      DumpOptions.default
        .withVerbose(v)
        .withShowCounter(sc)
        .withCompressOutput(sc)
        .withShowSchema(ss)
        .withDumpMode(dm)
  }

  private val countEntities =
    Opts.flag("count", "count entities").map(_ => DumpActionOpt.CountEntitiesOpt)

  private val showEntities: Opts[Unit] =
    Opts.flag("show", "show entities") // _ => ShowEntities)

  private val showEntitiesMax =
    (showEntities, maxStatements).mapN { case (_, max) => DumpActionOpt.ShowEntitiesOpt(max) }

  def verboseLevel: Opts[VerboseLevel] =
    Opts
      .option[String]("verbose", short = "v", help = s"verbose level ($showVerboseLevels)")
      .mapValidated(
        n =>
          VerboseLevel.fromString(n) match {
            case Some(v) => Validated.valid(v)
            case None    => Validated.invalidNel(s"Unknown value for verbose level: $n")
          }
      )
      .withDefault(Nothing)

  private val filterBySchema =
    (schemaPath, schemaFormat, verboseLevel).mapN(DumpActionOpt.FilterBySchemaOpt)

  private val action: Opts[DumpActionOpt] = countEntities orElse showEntitiesMax orElse filterBySchema

  private val dump: Opts[Dump] =
    Opts.subcommand("dump", "Process example dump file.") {
      (filePath, action, outPath, dumpOpts, processor, outputFormat).mapN(Dump)
    }

  override def main: Opts[IO[ExitCode]] =
    (processEntity orElse dump).map {
      case ProcessEntity(entity) => processEntity(entity)
      case Dump(filePath, action, outPath, verbose, processor, outputFormat) =>
        dump(filePath, action, outPath, verbose, processor, outputFormat)
    }

  def processEntity(entityStr: String): IO[ExitCode] =
    for {
      entity <- EntityFetcher.fetchEntity(entityStr)
      _      <- IO.println(s"entity: $entityStr")
      _      <- IO.println(s"entity ID: ${entity.getID()}")
      _      <- IO.println(s"entity Type: ${entity.getType()}")
    } yield ExitCode.Success

  def dump(
      filePath: Path,
      actionOpt: DumpActionOpt,
      maybeOutPath: Option[Path],
      dumpOptions: DumpOptions,
      processor: Processor,
      outputFormat: OutputFormat
  ): IO[ExitCode] = processor match {
    case Processor.Fs2 =>
      for {
        action <- actionOpt.toDumpAction
        is     <- IO { JavaFiles.newInputStream(filePath) }
        os <- maybeOutPath match {
          case Some(outPath) => Some(JavaFiles.newOutputStream(outPath, CREATE)).pure[IO]
          case None          => none[OutputStream].pure[IO]
        }
        refResults <- Ref[IO].of(DumpResults.initial)
        withEntry  <- getWithEntry(action, refResults)
        results    <- IODumpProcessor.process(is, os, withEntry, refResults, dumpOptions)
      } yield results.toExitCode
    case Processor.WDTK =>
      for {
        action <- actionOpt.toDumpAction
        exitCode <- action match {
          case DumpAction.FilterBySchema(schema) =>
            for {
              results <- DumpProcessor
                .dumpProcess(filePath, maybeOutPath, schema, dumpOptions, 0, outputFormat)
            } yield results.toExitCode
          case _ => IO.println(s"Not implemented yet") *> ExitCode.Error.pure[IO]
        }
      } yield exitCode
  }

  private def getWithEntry(
      action: DumpAction,
      refResults: Ref[IO, DumpResults]
  ): IO[EntityDoc => IO[Option[String]]] = (action.withEntry(refResults) _).pure[IO]

  private def checkSchema(matcher: Matcher, refResults: Ref[IO, DumpResults])(
      entity: EntityDoc
  ): IO[Option[String]] = {
    entity.entityDocument match {
      case e: EntityDocument => {
        if (matcher.matchStart(e).matches) {
          refResults.update(_.addMatched(e)) *>
            EntityDoc(e).asJsonStr().some.pure[IO]
        } else
          refResults.update(_.addEntity).void *>
            none.pure[IO]
      }
      case _ => none.pure[IO]
    }
  }

}
