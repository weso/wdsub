package es.weso.wdsub
import cats.implicits._
import fs2._
import fs2.io._
import fs2.compression._
import java.nio.file.Path
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import cats.effect._
import java.io.InputStream

// Attempt to create a dump processor based on fs2
object IODumpProcessor {

  def process(file: Path, withItem: ItemDocument => IO[Unit]): IO[Unit] = {
    val is: InputStream = ???
    val x = fs2.io.readInputStream(is.pure[IO],1024,true).through(Compression[IO].gunzip())
    ???
  }
    

}


