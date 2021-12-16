package es.weso.wdsub

import cats.effect._
import es.weso.wdshex._
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.datamodel.interfaces._
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor

import scala.collection.JavaConverters._

/**
  * WShEx processor
  *
  * @param schema ShEx schema
  * @param verbose verbose
  * @param timeout timeout in seconds or 0 if no timeout should be used
  */
class WDSubJsonProcessor(
  wShEx: WShEx,
  // maybeOut: Option[OutputStream],
  dumpWriter: Option[DumpWriter],
  verbose: Boolean,
  timeout: Int = 0
  ) extends EntityTimerProcessor(timeout) {

    private var totalEntities: Int = 0
    private var matchedEntities: Int = 0
    private val matcher = new Matcher(wShEx)
    private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName())
    // val jsonWriter:Option[JsonDumpWriter] = maybeOut.map(out => JsonDumpWriter(out))


    private def getProperty(sg: StatementGroup): PropertyValue = PropertyValue(sg.getProperty(), sg.getSubject())

    private def properties(item: ItemDocument): List[PropertyValue] =
      item.getStatementGroups().asScala.toList.map(getProperty)

    private def info(msg: String): Unit =
      if (verbose) logger.info(msg)


    override def processItemDocument(itemDocument: ItemDocument): Unit = {
        info(s"Item document: ${itemDocument.getEntityId().getId()} [${properties(itemDocument).map(_.toString()).mkString(",")}]")
        if (matcher.matchStart(itemDocument).matches) {
          matchedEntities += 1
          dumpWriter match {
            case Some(dw) => dw.writeItem(itemDocument)
            case None => None
          }
        }
        totalEntities += 1
      super.processItemDocument(itemDocument)
    }

    def getTotalEntities(): IO[Int] = IO { totalEntities }

    def getMatchedEntities(): IO[Int] = IO { matchedEntities }

    def startJson(): Unit = dumpWriter.map(_.start())
    def endJson(): Unit = dumpWriter.map(_.end())

    override def close(): Unit = {
      dumpWriter.map(_.close())
      super.close()
    }

}