package es.weso.wdsub

import org.wikidata.wdtk.datamodel.interfaces._
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory

class WShExProcessor(
  schema: Schema,
  verbose: Boolean
  ) extends EntityDocumentProcessor {

    var totalEntities: Int = 0
    var matchedEntities: Int = 0
    val matcher = new Matcher(schema)
    lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    
    def getProperty(sg: StatementGroup): PropertyValue = PropertyValue(sg.getProperty(), sg.getSubject())

    def properties(item: ItemDocument): List[PropertyValue] = 
      item.getStatementGroups().asScala.toList.map(getProperty)

    override def processItemDocument(itemDocument: ItemDocument): Unit = {
        if (verbose) 
          logger.info(s"Item document: ${itemDocument.getEntityId().getId()} [${properties(itemDocument).map(_.toString()).mkString(",")}]")
        if (!matcher.matchSomeShape(itemDocument).isEmpty) 
          matchedEntities += 1
        totalEntities += 1
    }

    def getTotalEntities(): Int = totalEntities

    def getMatchedEntities(): Int = matchedEntities

}