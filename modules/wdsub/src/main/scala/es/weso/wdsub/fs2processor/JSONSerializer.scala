package es.weso.wdsub.fs2processor

import cats.effect.IO
import es.weso.wbmodel.EntityDoc
import cats.implicits._

case class JSONSerializer() extends Serializer {
  def serialize(entityDoc: EntityDoc): IO[String] = entityDoc.asJsonStr().pure[IO]

  def sep = ",\n"
}
