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
import java.io.ByteArrayInputStream
import java.io.InputStream
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import cats.effect.unsafe.implicits.global

class IODumpTest extends FunSuite {

  test("Simple processing") {
    val q42 = new ItemIdValueImpl("Q42","http://www.wikidata.org/entity/")
    val p31 = new PropertyIdValueImpl("P31", "http://www.wikidata.org/entity/")
    val q515 = new ItemIdValueImpl("Q515","http://www.wikidata.org/entity/")
    val statementBuilder = 
      StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515)
    val itemDocument = 
      ItemDocumentBuilder.forItemId(q42).withStatement(statementBuilder.build()).build()

    val str = Item(itemDocument).asJsonStr()
    val is: InputStream = new ByteArrayInputStream(str.getBytes)
    val os: ByteArrayOutputStream = new ByteArrayOutputStream()
    def withItem(i: ItemDocument): Option[String] = None
    IODumpProcessor.process(
      is,os,withItem, 
      DumpOptions.default.withoutDecompressInput.withoutCompressOutput
    ).unsafeRunSync()
    assertEquals(os.toString(), str)
  }

}