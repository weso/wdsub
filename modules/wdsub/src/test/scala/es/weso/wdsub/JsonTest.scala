package es.weso.wdsub
import es.weso.rdf.nodes._
import es.weso.rdf._

import munit._
import org.wikidata.wdtk.datamodel.implementation._
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonFactoryBuilder
import com.fasterxml.jackson.core.JsonGenerator
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.charset.StandardCharsets
import es.weso.wdsub.writer.DumpWriter

class JsonTest extends FunSuite {

  test("Test Json generation with one item") {
    val q42  = new ItemIdValueImpl("Q42", "http://www.wikidata.org/entity/")
    val p31  = new PropertyIdValueImpl("P31", "http://www.wikidata.org/entity/")
    val q515 = new ItemIdValueImpl("Q515", "http://www.wikidata.org/entity/")
    val statementBuilder =
      StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515)
    val itemDocument =
      ItemDocumentBuilder.forItemId(q42).withStatement(statementBuilder.build()).build()
    val outStream: OutputStream = new ByteArrayOutputStream()
    val jsonWriter              = DumpWriter.fromOutputStream(outStream, DumpFormat.JSON)
    jsonWriter.start()
    jsonWriter.writeEntity(itemDocument)
    jsonWriter.end()
    val expected =
      """|[
         |{"type":"item","id":"Q42","labels":{},"descriptions":{},"aliases":{},"claims":{"P31":[{"rank":"normal","mainsnak":{"property":"P31","datatype":"wikibase-item","datavalue":{"value":{"id":"Q515","numeric-id":515,"entity-type":"item"},"type":"wikibase-entityid"},"snaktype":"value"},"type":"statement"}]},"sitelinks":{}},
         |]""".stripMargin

    val outStr = outStream.toString()
    /*      if (outStr==expected) println(s"Strings are equal")
      else {
        println(s"Strings are different")
      } */
    assertNoDiff(outStr, expected)
  }

  test("Test Json generation with two items") {
    val q42  = new ItemIdValueImpl("Q42", "http://www.wikidata.org/entity/")
    val p31  = new PropertyIdValueImpl("P31", "http://www.wikidata.org/entity/")
    val q515 = new ItemIdValueImpl("Q515", "http://www.wikidata.org/entity/")
    val statementBuilder =
      StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515)
    val itemDocument =
      ItemDocumentBuilder.forItemId(q42).withStatement(statementBuilder.build()).build()
    val outStream: OutputStream = new ByteArrayOutputStream()
    val jsonWriter              = DumpWriter.fromOutputStream(outStream, DumpFormat.JSON)
    jsonWriter.start()
    jsonWriter.writeEntity(itemDocument)
    jsonWriter.writeEntity(itemDocument)
    jsonWriter.end()
    val expected =
      """|[
         |{"type":"item","id":"Q42","labels":{},"descriptions":{},"aliases":{},"claims":{"P31":[{"rank":"normal","mainsnak":{"property":"P31","datatype":"wikibase-item","datavalue":{"value":{"id":"Q515","numeric-id":515,"entity-type":"item"},"type":"wikibase-entityid"},"snaktype":"value"},"type":"statement"}]},"sitelinks":{}},
         |{"type":"item","id":"Q42","labels":{},"descriptions":{},"aliases":{},"claims":{"P31":[{"rank":"normal","mainsnak":{"property":"P31","datatype":"wikibase-item","datavalue":{"value":{"id":"Q515","numeric-id":515,"entity-type":"item"},"type":"wikibase-entityid"},"snaktype":"value"},"type":"statement"}]},"sitelinks":{}},
         |]""".stripMargin

    assertNoDiff(outStream.toString(), expected)
  }

}
