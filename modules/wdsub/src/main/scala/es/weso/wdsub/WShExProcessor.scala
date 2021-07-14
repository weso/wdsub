package es.weso.wdsub

import cats.effect._
import org.wikidata.wdtk.datamodel.interfaces._
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.dumpfiles.EntityTimerProcessor
import java.io.OutputStream

/**
  * WShEx processor
  *
  * @param schema ShEx schema
  * @param verbose verbose
  * @param timeout timeout in seconds or 0 if no timeout should be used
  */
class WShExProcessor(
  schema: Schema,
  out: OutputStream,
  verbose: Boolean,
  timeout: Int = 0
  ) extends EntityTimerProcessor(timeout) {

    private var totalEntities: Int = 0
    private var matchedEntities: Int = 0
    private val matcher = new Matcher(schema)
    private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    
    private def getProperty(sg: StatementGroup): PropertyValue = PropertyValue(sg.getProperty(), sg.getSubject())

    private def properties(item: ItemDocument): List[PropertyValue] = 
      item.getStatementGroups().asScala.toList.map(getProperty)

    private def info(msg: String): Unit =   
      if (verbose) logger.info(msg)


    override def processItemDocument(itemDocument: ItemDocument): Unit = {
        info(s"Item document: ${itemDocument.getEntityId().getId()} [${properties(itemDocument).map(_.toString()).mkString(",")}]")
        if (matcher.matchSomeShape(itemDocument).size > 0) { 
          matchedEntities += 1
          out.write(itemDocument.toString().getBytes())
        }
        totalEntities += 1
    }

    def getTotalEntities(): IO[Int] = IO { totalEntities }

    def getMatchedEntities(): IO[Int] = IO { matchedEntities }

    override def close(): Unit = {
      out.close()
      super.close()
    }

}