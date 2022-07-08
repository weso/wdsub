package es.weso.wdsub

import cats.effect._
import org.wikidata.wdtk.datamodel.interfaces._
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor
import java.io.OutputStream
import es.weso.wdshex._
import es.weso.wshex.matcher.Matcher
import es.weso.wshex.WSchema
import es.weso.wshex.matcher.Matching
import es.weso.wshex.matcher.NoMatching

/**
  * WShEx processor
  *
  * @param schema ShEx schema
  * @param verbose verbose
  * @param timeout timeout in seconds or 0 if no timeout should be used
  */
abstract class WDSubProcessor(
    wShEx: WSchema,
    dumpWriter: Option[DumpWriter],
    opts: DumpOptions
) extends EntityDocumentDumpProcessor
    with EntityCounter {

  private val matcher     = Matcher(wShEx)
  private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName())

  protected def info(msg: String): Unit =
    if (opts.verbose) logger.info(msg)

  override def processItemDocument(itemDocument: ItemDocument): Unit = {

    matcher.matchStart(itemDocument) match {
      case _: Matching => {
        if (opts.verbose) {
          println(s"Item: ${itemDocument.getEntityId().getId()} matched")
        }
        incrementMatched()
        dumpWriter.map(dw => dw.writeItem(itemDocument))
        super.processItemDocument(itemDocument)
      }
      case NoMatching(es, ds) => {
        if (opts.verbose) {
          println(s"Item: ${itemDocument.getEntityId().getId()} doesn't match. \nErrors: $es")
        }
        incrementTotal()
      }
    }
  }

  override def open(): Unit = {
    resetCounter()
    dumpWriter.map(_.start())
  }

  override def close(): Unit = {
    if (opts.showCounter) {
      info(s"Counter: ${showCounter}")
    }
    dumpWriter.map(_.end())
    dumpWriter.map(_.close())
  }

}
