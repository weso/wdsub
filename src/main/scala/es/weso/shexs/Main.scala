package es.weso.shexs
import cats.effect._
import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import buildinfo._

object Main extends CommandIOApp (
  name="wdsub", 
  header = "Wikidata subsetting command line tool",
  version = BuildInfo.version
  ) {

  override def main: Opts[IO[ExitCode]] = ???
      
}