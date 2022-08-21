package es.weso.wdsub.fs2processor

import cats.effect._
import es.weso.wbmodel.EntityDoc
import cats.implicits._

case class PlainSerializer() extends Serializer {
  def serialize(entityDoc: EntityDoc): IO[String] =
    IO.println(s"Plain serializer") *> s"${entityDoc.entityDocument.getEntityId().getId()}".pure[IO]

  def sep = "\n"
}

object PlainSerializer {

  def make(): Resource[IO, PlainSerializer] = Resource.make(IO(PlainSerializer()))(s => ().pure[IO])

}
