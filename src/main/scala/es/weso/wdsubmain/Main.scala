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


sealed trait Processor {
  val name: String
}
case object WDTK extends Processor  { override val name = "WDTK"}
case object Fs2 extends Processor { override val name = "Fs2" } 

case class Dump(
  filePath: Path, 
  schemaPath: Path, 
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


  val dump: Opts[Dump] = 
    Opts.subcommand("dump", "Process example dump file.") {
      (filePath, outPath, schemaPath, verbose, processor).mapN(Dump)
  }  


  override def main: Opts[IO[ExitCode]] = 
    (processEntity orElse dump).map { 
      case ProcessEntity(entity) => processEntity(entity) 
      case Dump(filePath, outPath: Path, schemaPath, verbose, processor) => dump(filePath, outPath, schemaPath, verbose, processor)
    }

  def processEntity(entityStr: String): IO[ExitCode] = for {
    entity <- EntityFetcher.fetchEntity(entityStr)
    _ <- IO.println(s"entity: $entityStr")
    _ <- IO.println(s"entity ID: ${entity.getID()}")
    _ <- IO.println(s"entity Type: ${entity.getType()}")
  } yield ExitCode.Success

  def dump(
    filePath: Path, 
    outPath: Path, 
    schema: Path, 
    verbose: Boolean, 
    processor: Processor
    ): IO[ExitCode] = {
    for {
    results <- processor match {
      case Fs2 => IODumpProcessor.ioDumpProcess(filePath, outPath, schema, verbose, 0)
      case WDTK => DumpProcessor.dumpProcess(filePath, outPath, schema, verbose, 0)
    }
    _ <- IO.println(results)
    } yield ExitCode.Success
  }
      
}