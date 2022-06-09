package es.weso.wdsub
import org.wikidata.wdtk.datamodel.interfaces._

case class PropertyValue(property: PropertyIdValue, subject: EntityIdValue) {
  override def toString: String = s"${property.getId()}"
}
