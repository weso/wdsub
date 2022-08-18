package es.weso.wdsub.fs2processor

import cats.effect.IO
import es.weso.wbmodel.EntityDoc
import cats.implicits._
import org.wikidata.wdtk.rdf.{PropertyRegister, RdfSerializer}

case class RDFSerializer() extends Serializer {
  def serialize(entityDoc: EntityDoc): IO[String] = { 
    "not implemented RDF serializer".pure[IO] // RdfSerializer.
  } 
  // 

  def sep = "\n"
}
