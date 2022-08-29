package es.weso.wbmodel.serializer

import cats.effect._
import es.weso.wbmodel.EntityDoc
import cats.implicits._
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument


case class PlainSerializer() extends Serializer {
  def serialize(entityDocument: EntityDocument): IO[String] =
    IO.println(s"Plain serializer") *> s"${entityDocument.getEntityId().getId()}".pure[IO]

  def start = "".pure[IO]
  def end   = "".pure[IO]
  def sep   = "\n"
}

object PlainSerializer {

  def make(): Resource[IO, PlainSerializer] = Resource.make(IO(PlainSerializer()))(s => ().pure[IO])

}
