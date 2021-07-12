package es.weso.wdsub
import org.wikidata.wdtk.datamodel.interfaces._


case class PropertyValue(property: PropertyIdValue, value: EntityIdValue) {
    override def toString: String = s"${property.getId()}/${value.getId()}"
}
