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
import cats.effect._
import es.weso.wikibase._

class IODumpTest extends FunSuite {

  test("Iten json parser") {
    val q42 = new ItemIdValueImpl("Q42","http://www.wikidata.org/")
    val p31 = new PropertyIdValueImpl("P31", "http://www.wikidata.org/")
    val q515 = new ItemIdValueImpl("Q515","http://www.wikidata.org/")
    val statementBuilder = 
      StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515)
    val itemDocument = 
      ItemDocumentBuilder.forItemId(q42).withStatement(statementBuilder.build()).build()
    val item = Item(itemDocument)
    val jsonStr = item.asJsonStr()
    
    assertEquals(Item.fromJsonStr(jsonStr), Right(item))

  }

  test("Simple processing") {
    val q42 = new ItemIdValueImpl("Q42","http://www.wikidata.org/")
    val p31 = new PropertyIdValueImpl("P31", "http://www.wikidata.org/")
    val q515 = new ItemIdValueImpl("Q515","http://www.wikidata.org/")
    val statementBuilder = 
      StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515)
    val itemDocument = 
      ItemDocumentBuilder.forItemId(q42).withStatement(statementBuilder.build()).build()

    val str = Entity(itemDocument).asJsonStr()
    val is: InputStream = new ByteArrayInputStream(str.getBytes)
    val os: ByteArrayOutputStream = new ByteArrayOutputStream()
    def withEntity(e: Entity): IO[Option[String]] = IO(Some(e.asJsonStr()))
    val cmp = for {
      ref <- Ref[IO].of(DumpResults.initial)
      results <- IODumpProcessor.process(is,Some(os),withEntity, ref,
         DumpOptions.default.withoutDecompressInput.withoutCompressOutput) 
    } yield results
    cmp.unsafeRunSync()
    val expected = str + ",\n"
    val returned = os.toString()
    assertEquals(returned, expected)
  }

}