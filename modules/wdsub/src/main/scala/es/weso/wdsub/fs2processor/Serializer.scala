package es.weso.wdsub.fs2processor

import cats.effect._
import es.weso.wbmodel.EntityDoc
import es.weso.wdsub.DumpFormat
import org.eclipse.rdf4j.rio.RDFFormat
import java.io.OutputStream

abstract class Serializer {

  def serialize(entityDoc: EntityDoc): IO[String]

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
