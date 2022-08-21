package es.weso.wdsub.fs2processor

import cats.effect._
import es.weso.wbmodel.EntityDoc
import cats.implicits._

case class JSONSerializer() extends Serializer {
  def serialize(entityDoc: EntityDoc): IO[String] = entityDoc.asJsonStr().pure[IO]

  def sep = ",\n"
}

object JSONSerializer {

  def make(): Resource[IO, JSONSerializer] = Resource.make(IO(JSONSerializer()))(s => ().pure[IO])
}
