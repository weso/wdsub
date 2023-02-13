package es.weso.wdsubmain
import buildinfo._
import cats.data.Validated
import cats.effect._
import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import es.weso.utils.decline._
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
import es.weso.wikibase.EntityFetcher
import es.weso.wdsub.fs2processor._
import es.weso.wdsub.wdtk.DumpProcessor
import es.weso.utils.VerboseLevel._
import scala.concurrent.duration._ 
import java.time.Instant


case class ProcessEntity(entity: String)

object Main
    extends CommandIOApp(
      name = "wdsub",
      header = "Wikidata subsetting command line tool",
      version = BuildInfo.version
    ) {

  val processEntity: Opts[ProcessEntity] =
    Opts.subcommand("extract", "Show information about an entity.") {
      Opts.option[String]("entity", "Entity name", short = "e").map(ProcessEntity)
    }

  private lazy val filePath = Opts.argument[Path](metavar = "dumpFile")

  private val maxStatements = Opts.option[Int]("maxStatements", "max statements to show").orNone

  private val processor = validatedList("processor", Processor.availableProcessors, Some(Processor.WDTK))

  private val schemaFormat =
    validatedList("schemaFormat", SchemaFormat.availableSchemaFormats, Some(SchemaFormat.WShExC))

  private val schemaPath = Opts.option[Path]("schema", help = "ShEx schema", short = "s", metavar = "file")

  private val outPath = Opts.option[Path]("out", help = "output path", short = "o", metavar = "file").orNone

  private val verbose     = Opts.flag("verbose", "Verbose mode").orFalse
  private val showCounter = Opts.flag("showCounter", "Show counter at the end of process").orTrue
  private val showTime = Opts.flag("showTime", "Show processing time").orTrue
  private val showSchema  = Opts.flag("showSchema", "Show schema").orFalse
  private val mergeOrs  = Opts.flag("mergeOrs", "Merge items in ORs").orFalse

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
    validatedList("dumpMode", DumpMode.availableModes, Some(DumpMode.DumpOnlyMatched))

  private val dumpFormat =
    validatedList("dumpFormat", DumpFormat.availableFormats, Some(DumpFormat.JSON))

  private val dumpOpts: Opts[DumpOptions] =
    (verbose, showCounter, showTime, compressOutput, showSchema, dumpMode, mergeOrs, dumpFormat).mapN {
      case (v, sc, st, co, ss, dm, mo, df) =>
        DumpOptions.default
          .withVerbose(v)
          .withShowCounter(sc)
          .withShowTime(st)
          .withCompressOutput(sc)
          .withShowSchema(ss)
          .withDumpMode(dm)
          .withMergeOrs(mo)
          .withDumpFormat(df.toWBSerializeFormat)
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
      (filePath, action, outPath, dumpOpts, processor).mapN(Dump)
    }

  override def main: Opts[IO[ExitCode]] =
    (processEntity orElse dump).map {
      case ProcessEntity(entity) => processEntity(entity)
      case Dump(filePath, action, outPath, verbose, processor) =>
        dump(filePath, action, outPath, verbose, processor)
    }

  def processEntity(entityStr: String): IO[ExitCode] =
    for {
      entity <- EntityFetcher.fetchEntity(entityStr)
      _      <- IO.println(s"entity: $entityStr")
      _      <- IO.println(s"entity ID: ${entity.getID()}")
      _      <- IO.println(s"entity Type: ${entity.getType()}")
    } yield ExitCode.Success

  def getCurrentTime: IO[FiniteDuration] = 
    IO.realTime

  def showTime(d: FiniteDuration): String = {
    val t = Instant.ofEpochMilli(d.toMillis)
    t.toString
  }

  def dump(
      filePath: Path,
      actionOpt: DumpActionOpt,
      maybeOutPath: Option[Path],
      dumpOptions: DumpOptions,
      processor: Processor
  ): IO[ExitCode] = for {
    startTime <- getCurrentTime
    endTime <- getCurrentTime
    process <- processor match {
      case Processor.Fs2 =>
      for {
        action <- actionOpt.toDumpAction(dumpOptions)
        is     <- IO { JavaFiles.newInputStream(filePath) }
        os <- maybeOutPath match {
          case Some(outPath) => Some(JavaFiles.newOutputStream(outPath, CREATE_NEW)).pure[IO]
          case None          => none[OutputStream].pure[IO]
        }
        refResults <- Ref[IO].of(DumpResults.initial)
        withEntry = (action.withEntry(refResults) _)
        results <- IODumpProcessor.process(is, os, action.start, withEntry, action.sep, action.end, refResults, dumpOptions)
      } yield results.toExitCode
    case Processor.WDTK =>
      for {
        action <- actionOpt.toDumpAction(dumpOptions)
        exitCode <- action match {
          case da: DumpAction.FilterBySchema =>
            for {
              results <- DumpProcessor
                .dumpProcess(filePath, maybeOutPath, da.schema, dumpOptions)
            } yield results.toExitCode
          case _ => IO.println(s"Not implemented yet") *> ExitCode.Error.pure[IO]
        }
      } yield exitCode
  } 
    _ <- if (dumpOptions.showTime) 
       IO.println(s"""|Start time: ${showTime(startTime)}
                     |End time:   ${showTime(endTime)}
                     |Difference: ${(endTime - startTime).toCoarsest}
                     |""".stripMargin)
      else 
        IO.pure(())  
  } yield ExitCode.Success

}
