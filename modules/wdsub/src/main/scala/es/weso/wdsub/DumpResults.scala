package es.weso.wdsub

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument
import cats.effect.ExitCode

case class DumpResults(
    totalEntities: Int,
    matchedEntities: Int
) {
  def addMatched(e: EntityDocument) =
    this.copy(
      totalEntities = this.totalEntities + 1,
      matchedEntities = this.matchedEntities + 1
    )
  def addEntity         = this.copy(totalEntities = this.totalEntities + 1)
  override def toString = s"Matched/total entities = $matchedEntities/$totalEntities"

  def toExitCode: ExitCode = ExitCode.Success
}

object DumpResults {
  def initial: DumpResults = DumpResults(0, 0)
}
