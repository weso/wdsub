package es.weso.wdsub
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument
import org.wikidata.wdtk.datamodel.helpers.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.exc._

sealed abstract trait EntityParserError
case class  ParserError(e: Throwable) extends EntityParserError
case object End extends EntityParserError

case class Entity(entityDocument: EntityDocument) {
    def getID(): String = entityDocument.getEntityId().getId()
    def getType(): String = entityDocument.getEntityId().getEntityType()


    def asJsonStr(): String = {
        Entity.mapper.writeValueAsString(entityDocument)
    }
}

object Entity {

    private val mapper = new ObjectMapper()
    mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)

    def fromJsonStr(
        str: String, 
        site: String = "http://www.wikidata.org/entity/"
        ): Either[EntityParserError, Entity] = {
       try {
        val jsonDeserializer = new JsonDeserializer(site)
        Right(Entity(jsonDeserializer.deserializeEntityDocument(str)))
       } catch {
           case m: MismatchedInputException => Left(End)
           case e: Exception => Left(ParserError(e)) 
       }
    }


}