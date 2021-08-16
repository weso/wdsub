package es.weso.wdsubmain
import cats.effect._
import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import buildinfo._
import es.weso.wdsub._
import es.weso.rdf.nodes._
import es.weso.rdf._
import java.nio.file.Path
import cats.data.Validated
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument
import java.nio.file.{Files => JavaFiles, Paths}
import java.nio.file.StandardOpenOption._
import es.weso.wshex._

sealed trait Processor {
  val name: String
}
case object WDTK extends Processor  { override val name = "WDTK"}
case object Fs2 extends Processor { override val name = "Fs2" } 

sealed trait DumpAction
case class FilterBySchema(schema: Path) extends DumpAction
case object CountEntities extends DumpAction
case object ShowEntities extends DumpAction


case class Dump(
  filePath: Path, 
//  schemaPath: Path, 
  action: DumpAction,
  outPath: Path, 
  verbose: Boolean,
  processor: Processor
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

  val filePath = Opts.argument[Path](metavar="dumpFile")

  val processors = List(Fs2,WDTK)
  val processorNames = processors.map(_.name)
  val defaultProcessor = processors.head
  
  val processor = 
    Opts.option[String]("processor", help=s"Dump processor library. Possible values: ${processorNames.mkString(",")}").mapValidated(
      str => processors.find(_.name == str) match {
        case None => Validated.invalidNel(s"Invalid processor name: $str. Available processors: ${processorNames.mkString(",")}")
        case Some(p) => Validated.valid(p)
      }).withDefault(defaultProcessor)

  val schemaPath = Opts.option[Path]("schema", help="ShEx schema", short="s", metavar="file")

  val outPath = Opts.option[Path]("out", help="output path", short="o", metavar="file")

  val verbose = Opts.flag("verbose", "Verbose mode").orFalse

  val countEntities = Opts.flag("count", "count entities").map(_ => CountEntities)
  val showEntities = Opts.flag("show", "show entities").map(_ => ShowEntities)
  val filterBySchema = schemaPath.map(path => FilterBySchema(path))
  val action = countEntities orElse showEntities orElse filterBySchema 

  val dump: Opts[Dump] = 
    Opts.subcommand("dump", "Process example dump file.") {
      (filePath, action, outPath, verbose, processor).mapN(Dump)
  }  


  override def main: Opts[IO[ExitCode]] = 
    (processEntity orElse dump).map { 
      case ProcessEntity(entity) => processEntity(entity) 
      case Dump(filePath, action, outPath: Path, verbose, processor) => dump(filePath, action, outPath, verbose, processor)
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
    outPath: Path, 
    verbose: Boolean, 
    processor: Processor
    ): IO[ExitCode] = {
    for {
     is <- IO { JavaFiles.newInputStream(filePath) }
     os <- IO { JavaFiles.newOutputStream(outPath, CREATE) }
     refResults <- Ref[IO].of(DumpResults.initial)
     withEntry <- getWithEntry(action, refResults)
     results <- processor match {
      case Fs2 => IODumpProcessor.process(is, os, withEntry, refResults)
      case WDTK => action match {
        case FilterBySchema(schemaPath) => DumpProcessor.dumpProcess(filePath, outPath, schemaPath, verbose, 0)
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
    } yield checkSchema(matcher)
    case CountEntities => withEntryCount(refResults).pure[IO]
    case ShowEntities => withEntryShow(refResults).pure[IO]
  }

  private def withEntryCount(counter: Ref[IO,DumpResults]): Entity => IO[Option[String]] = _ => for {
    _ <- counter.update(_.addEntity) 
  } yield None

  private def withEntryShow(counter: Ref[IO,DumpResults]): Entity => IO[Option[String]] = entity => for {
    _ <- IO.println(entity.showShort)
    _ <- counter.update(_.addEntity) 
  } yield None

  private def checkSchema(matcher: Matcher)(entity: Entity): IO[Option[String]] = {
    entity.entityDocument match {
      case e: EntityDocument => {
        if (matcher.matchStart(e).matches) {
          Some(Entity(e).asJsonStr()).pure[IO]
        }
        else none.pure[IO]
      }
      case _ => none.pure[IO]
    }
  }

      
}