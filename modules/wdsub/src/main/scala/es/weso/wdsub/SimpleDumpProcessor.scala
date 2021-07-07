package es.weso.wdsub

import org.wikidata.wdtk.datamodel.interfaces._
import scala.collection.JavaConverters._

case class PropertyValue(property: PropertyIdValue, value: EntityIdValue) {
    override def toString: String = s"${property.getId()}/${value.getId()}"
}

class SimpleDumpProcessor extends EntityDocumentProcessor {
    
    def getProperty(sg: StatementGroup): PropertyValue = PropertyValue(sg.getProperty(), sg.getSubject())

    def properties(item: ItemDocument): List[PropertyValue] = 
      item.getStatementGroups().asScala.toList.map(getProperty)

    override def processItemDocument(itemDocument: ItemDocument): Unit = {
        println(s"Item document: ${itemDocument.getEntityId().getId()} [${properties(itemDocument).map(_.toString()).mkString(",")}]")
    }
}