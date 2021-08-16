package es.weso.wdsub
import org.wikidata.wdtk.datamodel.interfaces._
import org.wikidata.wdtk.datamodel.helpers.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.exc._
import cats.effect._
import collection.JavaConverters._
import es.weso.rbe.interval.IntLimit

sealed abstract trait EntityParserError
case class  ParserError(e: Throwable) extends EntityParserError
case object End extends EntityParserError

case class ShowEntityOptions(
    maxStatements: Option[Int],
    showAllValues: Boolean
) {
    def witMaxStatements(m: Option[Int]): ShowEntityOptions =
        this.copy(maxStatements = m)
}

object ShowEntityOptions {
    lazy val default: ShowEntityOptions = 
        ShowEntityOptions(
            maxStatements = None, 
            showAllValues = true
            ) 
}

case class Entity(entityDocument: EntityDocument) {

    private val mapper = new ObjectMapper()
    mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)

    def getID(): String = entityDocument.getEntityId().getId()
    def getType(): String = entityDocument.getEntityId().getEntityType()


    def asJsonStr(): String = {
        mapper.writeValueAsString(entityDocument)
    }

    def showValue(value: Value, options: ShowEntityOptions): String = value match {
      case g: GlobeCoordinatesValue => g.toString
      case e: EntityIdValue => e.getId() 
      case i: IriIdentifiedValue => i.getIri  
      case m: MonolingualTextValue => m.getText()
      case q: QuantityValue => q.getNumericValue().toString()
      case s: StringValue => s.getString
      case t: TimeValue => t.toString 
      case u: UnsupportedValue => u.toString
      case o => 
        if (options.showAllValues) 
            if (o == null) "no value"
            else o.toString
        else 
            "?" 
    }

    def showStatement(s: Statement, options: ShowEntityOptions): String = 
        s"${s.getMainSnak().getPropertyId().getId()}/${showValue(s.getValue(), options)}"

    private def getNumber(s: String): Int = {
        val p = "P(\\d+)".r
        p.findFirstMatchIn(s) match {
            case Some(nn) => nn.group(1).toInt
            case _ => 0
        }
    }    

    private def compareProperty(s1: Statement, s2: Statement): Boolean = 
        getNumber(s1.getMainSnak().getPropertyId().getId()) < getNumber(s2.getMainSnak().getPropertyId().getId())

    def showStatements(options: ShowEntityOptions): String = 
        entityDocument match {
            case s: StatementDocument => {
                val ss = s.getAllStatements().asScala.toList.sortWith(compareProperty)
                val ps = options.maxStatements match {
                    case Some(m) => ss.take(m)
                    case None => ss
                }
                ps.map(showStatement(_, options)).mkString(",")
            }
            case _ => "{}"
        }

    def show(options: ShowEntityOptions = ShowEntityOptions.default): String = 
        s"${entityDocument.getEntityId().getId()} ${showStatements(options)}"
}

object Entity {

    def fromJsonStr(
        str: String, 
        jsonDeserializer: JsonDeserializer
        ): IO[Entity] = IO { Entity(jsonDeserializer.deserializeEntityDocument(str)) }


}