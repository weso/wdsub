package es.weso.wdsub.fs2processor

import java.nio.file.Path
import es.weso.wshex.WShExFormat
import es.weso.wbmodel.EntityDoc
import cats.effect._
import cats._
import cats.implicits._
import es.weso.wdsub.DumpResults
import es.weso.wbmodel.ShowEntityOptions
import es.weso.wshex.WSchema
import es.weso.wshex.matcher.Matcher
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument
import es.weso.utils.VerboseLevel
import es.weso.utils.named._

sealed trait DumpAction {
  def withEntry(r: Ref[IO, DumpResults])(e: EntityDoc): IO[Option[String]]
}

object DumpAction {

  def filterBySchema(schemaPath: Path, schemaFormat: WShExFormat, verbosity: VerboseLevel): IO[FilterBySchema] =
    WSchema.fromPath(schemaPath, schemaFormat, VerboseLevel.Info).map(FilterBySchema)

  case class FilterBySchema(schema: WSchema) extends DumpAction {

    val matcher = Matcher(schema)
    override def withEntry(refResults: Ref[IO, DumpResults])(entity: EntityDoc): IO[Option[String]] =
      entity.entityDocument match {
        case e: EntityDocument => {
          if (matcher.matchStart(e).matches) {
            refResults.update(_.addMatched(e)) *>
              EntityDoc(e).asJsonStr().some.pure[IO]
          } else
            refResults.update(_.addEntity).void *>
              none.pure[IO]
        }
        case _ => none.pure[IO]
      }

  }

  case object CountEntities extends DumpAction {

    override def withEntry(counter: Ref[IO, DumpResults])(e: EntityDoc): IO[Option[String]] =
      for {
        _ <- counter.update(_.addEntity)
      } yield None
  }

  case class ShowEntities(maxStatements: Option[Int]) extends DumpAction {

    override def withEntry(ref: Ref[IO, DumpResults])(entity: EntityDoc): IO[Option[String]] =
      for {
        _ <- IO.println(entity.show(ShowEntityOptions.default.witMaxStatements(maxStatements)))
        _ <- ref.update(_.addEntity)
      } yield None

  }

}
