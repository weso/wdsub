package es.weso.wdsub

import cats.effect._
import es.weso.wdshex._
import org.eclipse.rdf4j.rio.RDFFormat
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.datamodel.interfaces._
import org.wikidata.wdtk.rdf.{PropertyRegister, RdfSerializer}

import java.io.OutputStream
import scala.collection.JavaConverters._

/**
  * WShEx processor
  *
  * @param schema ShEx schema
  * @param verbose verbose
  * @param timeout timeout in seconds or 0 if no timeout should be used
  */
class WDSubRDFProcessor(
  wShEx: WShEx,
  format: RDFFormat,
  output: OutputStream,
  sites: Sites,
  propertyRegister: PropertyRegister,
  verbose: Boolean,
  ) extends RdfSerializer(format, output, sites, propertyRegister) {

    private var totalEntities: Int = 0
    private var matchedEntities: Int = 0
    private val matcher = new Matcher(wShEx)
    private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName())

    private def getProperty(sg: StatementGroup): PropertyValue = PropertyValue(sg.getProperty(), sg.getSubject())

    private def properties(item: ItemDocument): List[PropertyValue] = 
      item.getStatementGroups().asScala.toList.map(getProperty)

    private def info(msg: String): Unit =   
      if (verbose) logger.info(msg)


    override def processItemDocument(itemDocument: ItemDocument): Unit = {
        info(s"Item document: ${itemDocument.getEntityId().getId()} [${properties(itemDocument).map(_.toString()).mkString(",")}]")
        if (matcher.matchStart(itemDocument).matches) { 
          matchedEntities += 1
          super.processItemDocument(itemDocument)
        }
        totalEntities += 1
    }

    def getTotalEntities(): IO[Int] = IO { totalEntities }

    def getMatchedEntities(): IO[Int] = IO { matchedEntities }

    override def close(): Unit = {
      super.close()
    }

}