package es.weso.wdsub

import cats.effect._
import org.wikidata.wdtk.datamodel.interfaces._
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor
import java.io.OutputStream
import es.weso.wshex._

/**
  * WShEx processor
  *
  * @param schema ShEx schema
  * @param verbose verbose
  * @param timeout timeout in seconds or 0 if no timeout should be used
  */
class WDSubProcessor(
  wShEx: WShEx,
  maybeOut: Option[OutputStream],
  verbose: Boolean,
  timeout: Int = 0
  ) extends EntityTimerProcessor(timeout) {

    private var totalEntities: Int = 0
    private var matchedEntities: Int = 0
    private val matcher = new Matcher(wShEx)
    private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName())
    val jsonWriter:Option[JsonDumpWriter] = maybeOut.map(out => JsonDumpWriter(out))

    
    private def getProperty(sg: StatementGroup): PropertyValue = PropertyValue(sg.getProperty(), sg.getSubject())

    private def properties(item: ItemDocument): List[PropertyValue] = 
      item.getStatementGroups().asScala.toList.map(getProperty)

    private def info(msg: String): Unit =   
      if (verbose) logger.info(msg)


    override def processItemDocument(itemDocument: ItemDocument): Unit = {
        info(s"Item document: ${itemDocument.getEntityId().getId()} [${properties(itemDocument).map(_.toString()).mkString(",")}]")
        if (matcher.matchStart(itemDocument).matches) { 
          matchedEntities += 1
          jsonWriter match { 
            case Some(jw) => jw.writeItem(itemDocument)
            case None => None
          }
        }
        totalEntities += 1
    }

    def getTotalEntities(): IO[Int] = IO { totalEntities }

    def getMatchedEntities(): IO[Int] = IO { matchedEntities }

    def startJson(): Unit = jsonWriter.map(_.start())
    def endJson(): Unit = jsonWriter.map(_.end())

    override def close(): Unit = {
      maybeOut match {
        case None => None
        case Some(out) => out.close()
      }
      super.close()
    }

}