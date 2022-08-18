package es.weso.wdsubmain

import es.weso.wdsub.DumpOptions
import cats.effect.IO
import es.weso.wdsub.fs2processor.DumpAction
import es.weso.wdsub.fs2processor.DumpAction._
import java.nio.file.Path
import es.weso.utils.VerboseLevel
import cats.implicits._
import es.weso.utils.named._

sealed abstract class DumpActionOpt extends Named {
  def toDumpAction(opts: DumpOptions): IO[DumpAction]
}
object DumpActionOpt {
  case class FilterBySchemaOpt(path: Path, format: SchemaFormat, verbosity: VerboseLevel) extends DumpActionOpt {
    val name = "Filter"
    def toDumpAction(opts: DumpOptions): IO[DumpAction] =
      DumpAction.filterBySchema(path, format.wshexFormat, verbosity, opts)
  }
  case object CountEntitiesOpt extends DumpActionOpt {
    val name                                            = "countEntities"
    def toDumpAction(opts: DumpOptions): IO[DumpAction] = DumpAction.CountEntities.pure[IO]
  }
  case class ShowEntitiesOpt(maxEntities: Option[Int]) extends DumpActionOpt {
    val name                                            = "showEntities"
    def toDumpAction(opts: DumpOptions): IO[DumpAction] = DumpAction.ShowEntities(maxEntities).pure[IO]
  }
}
