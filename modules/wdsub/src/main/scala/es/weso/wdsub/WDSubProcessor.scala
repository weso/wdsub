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
    dumpWriter: Option[DumpWriter],
    opts: DumpOptions
) extends EntityDocumentDumpProcessor
    with EntityCounter {

  private val matcher     = new Matcher(wShEx)
  private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName())

  protected def info(msg: String): Unit =
    if (opts.verbose) logger.info(msg)

  override def processItemDocument(itemDocument: ItemDocument): Unit = {
    if (matcher.matchStart(itemDocument).matches) {
      incrementMatched()
      dumpWriter.map(dw => dw.writeItem(itemDocument))
      super.processItemDocument(itemDocument)
    } else incrementTotal()
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
