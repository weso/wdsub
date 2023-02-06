package es.weso.wdsub
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument
import java.io.ByteArrayOutputStream
import com.fasterxml.jackson.databind.ObjectMapper
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import com.fasterxml.jackson.core.JsonGenerator
import org.wikidata.wdtk.datamodel.implementation.ItemDocumentImpl
import org.wikidata.wdtk.datamodel.helpers.JsonDeserializer

case class Item(itemDocument: ItemDocument) {

    def asJsonStr(): String = {
       Item.mapper.writeValueAsString(itemDocument)
    }
}

object Item {

    val mapper = new ObjectMapper()
    mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)

    def fromJsonStr(str: String): Either[String, Item] = {
       try {
        val jsonDeserializer = new JsonDeserializer("http://www.wikidata.org/")   
        Right(Item(jsonDeserializer.deserializeItemDocument(str)))
       } catch {
           case e: Exception => Left(s"Error parsing item: ${e.getMessage()}\nItem to parse:${str}\n")
       }
    }
}