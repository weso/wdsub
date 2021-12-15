package es.weso.wdsubmain
import buildinfo._
import cats.data.Validated
import cats.effect._
import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import es.weso.wdshex._
import es.weso.wdsub._
import es.weso.wikibase._
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument

import java.io._
import java.nio.file.StandardOpenOption._
import java.nio.file.{Path, Files => JavaFiles}

sealed trait Processor {
  val name: String
}
case object WDTK extends Processor  { override val name = "WDTK"}
case object Fs2 extends Processor { override val name = "Fs2" } 

sealed trait DumpAction
case class FilterBySchema(schema: Path) extends DumpAction
case object CountEntities extends DumpAction
case class ShowEntities(maxStatements: Option[Int]) extends DumpAction

case class Dump(
  filePath: Path, 
  action: DumpAction,
  outPath: Option[Path], 
  verbose: Boolean,
  processor: Processor,
  outputFormat: OutputFormat
  )

case class ProcessEntity(entity: String)

object Main extends CommandIOApp (
  name="wdsub", 
  header = "Wikidata subsetting command line tool",
  version = BuildInfo.version
  ) {
  
  lazy val DUMP_FILE = "modules/wdsub/src/resources/sample-dump-20150815.json.gz"    

  val processEntity: Opts[ProcessEntity] = 
   Opts.subcommand("extract", "Show information about an entity.") {
   Opts.option[String]("entity", "Entity name", short="e").map(ProcessEntity)
   }  

  private val filePath = Opts.argument[Path](metavar="dumpFile")
  private val processors = List(Fs2,WDTK)
  private val processorNames = processors.map(_.name)
  private val defaultProcessor = processors.head

  private val outputFormats = List(JsonDump,TurtleDump)
  private val outputFormatNames = outputFormats.map(_.name)
  private val defaultOutputFormat = outputFormats.head

  private val maxStatements = Opts.option[Int]("maxStatements", "max statements to show").orNone
  
  private val processor =
    Opts.option[String]("processor", help=s"Dump processor library. Possible values: ${processorNames.mkString(",")}").mapValidated(
      str => processors.find(_.name == str) match {
        case None => Validated.invalidNel(s"Invalid processor name: $str. Available processors: ${processorNames.mkString(",")}")
        case Some(p) => Validated.valid(p)
      }).withDefault(defaultProcessor)

  private val outputFormat =
    Opts.option[String]("outputFormat", help=s"Output format. Possible values: ${outputFormatNames.mkString(",")}").mapValidated(
      str => outputFormats.find(_.name == str) match {
        case None => Validated.invalidNel(s"Invalid output format: $str. Available processors: ${outputFormatNames.mkString(",")}")
        case Some(p) => Validated.valid(p)
      }).withDefault(defaultOutputFormat)

  private val schemaPath = Opts.option[Path]("schema", help="ShEx schema", short="s", metavar="file")

  private val outPath = Opts.option[Path]("out", help="output path", short="o", metavar="file").orNone

  private val verbose = Opts.flag("verbose", "Verbose mode").orFalse

  private val countEntities =
    Opts.flag("count", "count entities").map(_ => CountEntities)
  

/*  def f(x: Unit, maxStatements: Option[Int]): ShowEntities =
    ShowEntities(maxStatements) */

  private val showEntities: Opts[Unit] =
    Opts.flag("show", "show entities") // _ => ShowEntities)

  private val showEntitiesMax =
    (showEntities, maxStatements).mapN{ case (_,max) => ShowEntities(max) }
  
  private val filterBySchema =
    schemaPath.map(path => FilterBySchema(path))
  
  private val action: Opts[DumpAction] = countEntities orElse showEntitiesMax orElse filterBySchema

  private val dump: Opts[Dump] =
    Opts.subcommand("dump", "Process example dump file.") {
      (filePath, action, outPath, verbose, processor, outputFormat).mapN(Dump)
  }  


  override def main: Opts[IO[ExitCode]] = 
    (processEntity orElse dump).map { 
      case ProcessEntity(entity) => processEntity(entity) 
      case Dump(filePath, action, outPath, verbose, processor, outputFormat) => dump(filePath, action, outPath, verbose, processor, outputFormat)
    }

  def processEntity(entityStr: String): IO[ExitCode] = for {
    entity <- EntityFetcher.fetchEntity(entityStr)
    _ <- IO.println(s"entity: $entityStr")
    _ <- IO.println(s"entity ID: ${entity.getID()}")
    _ <- IO.println(s"entity Type: ${entity.getType()}")
  } yield ExitCode.Success

  def dump(
    filePath: Path, 
    action: DumpAction, 
    maybeOutPath: Option[Path], 
    verbose: Boolean, 
    processor: Processor,
    outputFormat: OutputFormat
    ): IO[ExitCode] = {
    for {
     is <- IO { JavaFiles.newInputStream(filePath) }
     os <- maybeOutPath match { 
       case Some(outPath) => Some(JavaFiles.newOutputStream(outPath, CREATE)).pure[IO]
       case None => none[OutputStream].pure[IO]
     }
     refResults <- Ref[IO].of(DumpResults.initial)
     withEntry <- getWithEntry(action, refResults)
     results <- processor match {
      case Fs2 => IODumpProcessor.process(is, os, withEntry, refResults)
      case WDTK => action match {
        case FilterBySchema(schemaPath) => DumpProcessor.dumpProcess(filePath, maybeOutPath, schemaPath, verbose, 0, outputFormat)
        case _ => IO.println(s"Not implemented yet")
      }
     }
     _ <- IO.println(s"End of dump processing: $results")
    } yield ExitCode.Success
  }


  private def getWithEntry(
    action: DumpAction, 
    refResults: Ref[IO, DumpResults]
    ): IO[Entity => IO[Option[String]]] = action match {
    case FilterBySchema(schemaPath) => for {
      wshex <- WShEx.fromPath(schemaPath)
      matcher = new Matcher(wShEx = wshex)
    } yield checkSchema(matcher, refResults)
    case CountEntities => withEntryCount(refResults).pure[IO]
    case ShowEntities(max) => withEntryShow(refResults, max).pure[IO]
  }

  private def withEntryCount(counter: Ref[IO,DumpResults]): Entity => IO[Option[String]] = _ => for {
    _ <- counter.update(_.addEntity) 
  } yield None

  private def withEntryShow(counter: Ref[IO,DumpResults], maxEntities: Option[Int]): Entity => IO[Option[String]] = entity => for {
    _ <- IO.println(entity.show(ShowEntityOptions.default.witMaxStatements(maxEntities)))
    _ <- counter.update(_.addEntity) 
  } yield None

  private def checkSchema(matcher: Matcher, refResults: Ref[IO,DumpResults])(entity: Entity): IO[Option[String]] = {
    entity.entityDocument match {
      case e: EntityDocument => {
        if (matcher.matchStart(e).matches) {
          refResults.update(_.addMatched) *>
          Some(Entity(e).asJsonStr()).pure[IO]
        }
        else refResults.update(_.addEntity) *> 
        none.pure[IO]
      }
      case _ => none.pure[IO]
    }
  }

      
}