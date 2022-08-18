package es.weso.wdsub.fs2processor

import cats.effect.IO
import es.weso.wbmodel.EntityDoc

abstract class Serializer {

  def serialize(entityDoc: EntityDoc): IO[String]

  // Line separator
  def sep: String
}
