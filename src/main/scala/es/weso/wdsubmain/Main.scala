package es.weso.wdsubmain
import cats.effect._
import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import buildinfo._
import es.weso.wdsub._

case class Dump(fileName: Option[String])
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

  val fileName = Opts.option[String](
    "fileName", "Local dump file name", short = "f"
  ).orNone 

  val dump: Opts[Dump] = 
    Opts.subcommand("dump", "Process example dump file.") {
      (fileName).map(Dump)
  }  


  override def main: Opts[IO[ExitCode]] = 
    (processEntity orElse dump).map { 
      case ProcessEntity(entity) => processEntity(entity) 
      case Dump(fileName) => dump(fileName)
    }


  def processEntity(entityStr: String): IO[ExitCode] = for {
    entity <- EntityFetcher.fetchEntity(entityStr)
    _ <- IO.println(s"entity: $entityStr")
    _ <- IO.println(s"entity ID: ${entity.getID()}")
    _ <- IO.println(s"entity Type: ${entity.getType()}")
  } yield ExitCode.Success

  def dump(optFileName: Option[String]): IO[ExitCode] = 
    DumpProcessor.dumpProcess(optFileName.getOrElse(DUMP_FILE)) *>
    IO.pure(ExitCode.Success)
      
}