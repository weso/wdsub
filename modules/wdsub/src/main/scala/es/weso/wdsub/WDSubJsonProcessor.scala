package es.weso.wdsub

import cats.effect._
import es.weso.wshex._
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.datamodel.interfaces._
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor

import scala.collection.JavaConverters._
import java.io.OutputStream

/**
  * WShEx processor
  *
  * @param schema ShEx schema
  * @param verbose verbose
  * @param timeout timeout in seconds or 0 if no timeout should be used
  */
class WDSubJsonProcessor(
    wShEx: WSchema,
    maybeOut: Option[OutputStream],
    opts: DumpOptions,
    timeout: Int = 0
) extends WDSubProcessor(wShEx, maybeOut.map(out => JsonDumpWriter(out)), opts) {

  var totalEntities: Int   = 0
  var matchedEntities: Int = 0

  private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName())

  private def getProperty(sg: StatementGroup): PropertyValue =
    PropertyValue(sg.getProperty(), sg.getSubject())

  private def properties(item: ItemDocument): List[PropertyValue] =
    item.getStatementGroups().asScala.toList.map(getProperty)

  override def processItemDocument(itemDocument: ItemDocument): Unit = {
    info(
      s"Item document($showCounter): ${itemDocument.getEntityId().getId()} [${properties(
        itemDocument
      ).map(_.toString()).mkString(",")}]"
    )
    super.processItemDocument(itemDocument)
  }

}
