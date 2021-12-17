package es.weso.wdsub

import cats.effect._
import es.weso.wdshex._
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
  wShEx: WShEx,
  maybeOut: Option[OutputStream],
  verbose: Boolean,
  timeout: Int = 0
  ) extends WDSubProcessor(wShEx, verbose) {

    private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName())
    private lazy val dumpWriter: Option[DumpWriter] = maybeOut.map(out => JsonDumpWriter(out))

    private def getProperty(sg: StatementGroup): PropertyValue = PropertyValue(sg.getProperty(), sg.getSubject())

    private def properties(item: ItemDocument): List[PropertyValue] =
      item.getStatementGroups().asScala.toList.map(getProperty)

    override def processItemDocument(itemDocument: ItemDocument): Unit = {
      info(s"Item document: ${itemDocument.getEntityId().getId()} [${properties(itemDocument).map(_.toString()).mkString(",")}]")
      dumpWriter match {
        case Some(dw) => dw.writeItem(itemDocument)
        case None => ()
      }
      super.processItemDocument(itemDocument)
    }

    override def open(): Unit = dumpWriter.map(_.start())

    override def close(): Unit = {
      dumpWriter.map(_.end())
      dumpWriter.map(_.close())
    }

}