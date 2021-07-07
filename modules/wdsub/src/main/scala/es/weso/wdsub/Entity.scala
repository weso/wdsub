package es.weso.wdsub
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument


case class Entity(entityDocument: EntityDocument) {
    def getID(): String = entityDocument.getEntityId().getId()
    def getType(): String = entityDocument.getEntityId().getEntityType()
}