package es.weso.wbmodel.serializer

import cats.effect._
import es.weso.wbmodel.EntityDoc
import cats.implicits._
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonGenerator

case class JSONSerializer() extends Serializer {

  private val mapper = new ObjectMapper()
  mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)

  def serialize(entityDoc: EntityDocument): IO[String] = mapper.writeValueAsString(entityDoc).pure[IO]

  def start = "[\n".pure[IO]
  def sep   = ",\n"

  def end = "]".pure[IO]
}

object JSONSerializer {

  def make(): Resource[IO, JSONSerializer] = Resource.make(IO(JSONSerializer()))(s => ().pure[IO])
}
