package es.weso.wdsub

import cats.effect._
import org.wikidata.wdtk.datamodel.interfaces._
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor
import java.io.OutputStream
import es.weso.wdshex._

/**
  * WShEx processor
  *
  * @param schema ShEx schema
  * @param verbose verbose
  * @param timeout timeout in seconds or 0 if no timeout should be used
  */
abstract class WDSubProcessor(
  wShEx: WShEx,
  verbose: Boolean,
  ) extends EntityDocumentDumpProcessor {

    private var totalEntities: Int = 0
    private var matchedEntities: Int = 0
    private val matcher = new Matcher(wShEx)
    private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName())

    protected def info(msg: String): Unit =
      if (verbose) logger.info(msg)

    override def processItemDocument(itemDocument: ItemDocument): Unit = {
        if (matcher.matchStart(itemDocument).matches) { 
          matchedEntities += 1
          super.processItemDocument(itemDocument)
        }
        totalEntities += 1
    }

    def getTotalEntities(): IO[Int] = IO { totalEntities }

    def getMatchedEntities(): IO[Int] = IO { matchedEntities }

}