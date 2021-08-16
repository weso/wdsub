package es.weso.wdsub
import org.wikidata.wdtk.datamodel.interfaces._
import org.wikidata.wdtk.datamodel.helpers.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.exc._
import cats.effect._
import collection.JavaConverters._

sealed abstract trait EntityParserError
case class  ParserError(e: Throwable) extends EntityParserError
case object End extends EntityParserError

case class Entity(entityDocument: EntityDocument) {

    private val mapper = new ObjectMapper()
    mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)

    def getID(): String = entityDocument.getEntityId().getId()
    def getType(): String = entityDocument.getEntityId().getEntityType()


    def asJsonStr(): String = {
        mapper.writeValueAsString(entityDocument)
    }

    def showValue(value: Value): String = value match {
      case e: EntityIdValue => e.getId() 
      case m: MonolingualTextValue => m.getText()
//      case p: PropertyIdValue => p.getId()
      case _ => "other"
    }

    def showStatement(s: Statement): String = 
        s"${s.getMainSnak().getPropertyId().getId()}/${showValue(s.getValue())}"

    def showStatements: String = 
        entityDocument match {
            case s: StatementDocument => s.getAllStatements().asScala.toList.map(showStatement(_)).mkString(",")
            case _ => "{}"
        }

    def showShort: String = 
        s"${entityDocument.getEntityId().getId()} $showStatements"
}

object Entity {

    def fromJsonStr(
        str: String, 
        jsonDeserializer: JsonDeserializer
//                site: String = 

        ): IO[Entity] = IO { Entity(jsonDeserializer.deserializeEntityDocument(str)) }


}