package es.weso.wshex

import es.weso.rbe.interval.Unbounded
import es.weso.rdf.nodes._
import es.weso.rdf._
import es.weso.wbmodel._
import munit._
import org.wikidata.wdtk.datamodel.implementation._
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder
import es.weso.wshex._
import es.weso.wshex.matcher._

object IRIHelpers {

  val wde = IRI("http://www.wikidata.org/entity/")

  def p(n: Int): IRI =
    wde + ("P" + n.toString)

  def q(n: Int): IRI =
    wde + ("Q" + n.toString)

}

class WShExTest extends FunSuite {
  import IRIHelpers._

  val schemaStr = """|prefix :  <http://www.wikidata.org/entity/>
                     |
                     |start = @<City>
                     |
                     |<City> { 
                     |  :P31 [ :Q515 ]
                     |}""".stripMargin

  test("Match shape".only) {
    val q42  = new ItemIdValueImpl("Q42", "http://www.wikidata.org/entity/")
    val p31  = new PropertyIdValueImpl("P31", "http://www.wikidata.org/entity/")
    val q515 = new ItemIdValueImpl("Q515", "http://www.wikidata.org/entity/")
    val statementBuilder =
      StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515)
    val itemDocument =
      ItemDocumentBuilder.forItemId(q42).withStatement(statementBuilder.build()).build()
    val expected = EntityDoc(itemDocument)  

    println(s"#### Expected: $itemDocument")
    Matcher.unsafeFromString(schemaStr).fold(
      e => fail(s"Error parsing schema: $e\nSchemaStr: $schemaStr"),
      matcher => matcher.matchStart(itemDocument) match {
        case m: Matching => assertEquals(m.entity, expected)
        case nm: NoMatching => fail(s"No matching: $nm")
      }
    )
  }

}
