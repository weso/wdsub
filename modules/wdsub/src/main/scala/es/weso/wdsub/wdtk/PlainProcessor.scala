package es.weso.wdsub.wdtk

import cats.effect._
import es.weso.wshex._
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.datamodel.interfaces._
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor
import es.weso.wdsub.writer._
import es.weso.wdsub.DumpOptions

import scala.collection.JavaConverters._
import java.io.OutputStream

/**
  * WShEx processor
  *
  * @param schema ShEx schema
  * @param verbose verbose
  * @param timeout timeout in seconds or 0 if no timeout should be used
  */
class PlainProcessor(
    wShEx: WSchema,
    maybeOut: Option[OutputStream],
    opts: DumpOptions,
) extends WDTKProcessor(wShEx, maybeOut.map(out => PlainDumpWriter(out)), opts) 
  with EntityCounter {

    var totalEntities: Int = 0
    var matchedEntities: Int = 0
}
