package es.weso.wbmodel.serializer

import cats.effect._
import es.weso.wbmodel.EntityDoc
import es.weso.wdsub.DumpFormat
import org.eclipse.rdf4j.rio.RDFFormat
import java.io.OutputStream
import java.nio.charset.Charset
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument

abstract class Serializer {
  import cats.effect.unsafe.implicits.global

  def serialize(entityDocument: EntityDocument): IO[String]

  def serialize(entityDoc: EntityDoc): IO[String] = 
    serialize(entityDoc.entityDocument)

  private def writeOs(str: String, os: OutputStream): Unit =
      os.write(str.getBytes(Charset.defaultCharset()))

  def serializeOutputStream(entityDocument: EntityDocument, os: OutputStream): Unit = {
    writeOs(serialize(entityDocument).unsafeRunSync(), os)
  }

  // Beginning string
  def start: IO[String]

  def startOutputStream(os: OutputStream): Unit = 
    writeOs(start.unsafeRunSync(), os)
  
  // Last string
  def end: IO[String]

  def endOutputStream(os: OutputStream): Unit = 
    writeOs(end.unsafeRunSync(), os)

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
