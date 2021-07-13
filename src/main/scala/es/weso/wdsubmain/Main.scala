package es.weso.wdsubmain
import cats.effect._
import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import buildinfo._
import es.weso.wdsub._
import es.weso.rdf.nodes._
import es.weso.rdf._

case class Dump(fileName: Option[String], verbose: Boolean)
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

  val verbose = Opts.flag("verbose", "Verbose mode").orFalse


  val dump: Opts[Dump] = 
    Opts.subcommand("dump", "Process example dump file.") {
      (fileName, verbose).mapN(Dump)
  }  


  override def main: Opts[IO[ExitCode]] = 
    (processEntity orElse dump).map { 
      case ProcessEntity(entity) => processEntity(entity) 
      case Dump(fileName, verbose) => dump(fileName, verbose)
    }


  def processEntity(entityStr: String): IO[ExitCode] = for {
    entity <- EntityFetcher.fetchEntity(entityStr)
    _ <- IO.println(s"entity: $entityStr")
    _ <- IO.println(s"entity ID: ${entity.getID()}")
    _ <- IO.println(s"entity Type: ${entity.getType()}")
  } yield ExitCode.Success

  def dump(optFileName: Option[String], verbose: Boolean): IO[ExitCode] = {

    // TODO: Remove hardcoded Schema
    val shape = Shape(TripleConstraint(IRI("http://www.wikidata.org/entity/P31"), 
          Some(ValueSet(List(IRIValue(IRI("http://www.wikidata.org/entity/Q515")))))))

    val schema: Schema = Schema(
        pm = PrefixMap.empty,
        shapes = List(shape)
    )

    for {
     results <- DumpProcessor.dumpProcess(optFileName.getOrElse(DUMP_FILE), schema, verbose, 0)
    _ <- IO.println(results)
    } yield ExitCode.Success
  }
      
}