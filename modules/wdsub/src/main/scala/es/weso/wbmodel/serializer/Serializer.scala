package es.weso.wbmodel.serializer

import cats.effect._
import es.weso.wbmodel.EntityDoc
import es.weso.wdsub.DumpFormat
import org.eclipse.rdf4j.rio.RDFFormat
import java.io.OutputStream

abstract class Serializer {

  def serialize(entityDoc: EntityDoc): IO[String]

  def serializeOutputStream(entityDoc: EntityDoc, os: OutputStream): Unit = {
    import cats.effect.unsafe.implicits.global
    val str = serialize(entityDoc).unsafeRunSync()
    os.write(str.getBytes(Charset.forName("UTF-8")))
  }

  // Beginning string
  def start: IO[String]

  // Last string
  def end: IO[String]

  // Line separator
  def sep: String
}

object Serializer {

  def makeSerializer(format: DumpFormat): Resource[IO, Serializer] = format match {
    case DumpFormat.JSON   => JSONSerializer.make()
    case DumpFormat.Turtle => RDFSerializer.makeSerializer(RDFFormat.TURTLE)
    case DumpFormat.Plain  => PlainSerializer.make()
  }
}
