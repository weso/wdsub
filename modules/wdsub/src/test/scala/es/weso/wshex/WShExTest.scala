package es.weso.wshex

import es.weso.rbe.interval.Unbounded
import es.weso.rdf.nodes._
import es.weso.rdf._
import es.weso.wbmodel.PropertyId
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

  val shape = WShape(
    None,
    false,
    List(),
    Some(
      TripleConstraintLocal(
        PropertyId.fromIRI(IRI("http://www.wikidata.org/entity/P31")),
        ValueSet(None, List(IRIValueSetValue(IRI("http://www.wikidata.org/entity/Q515")))),
        1,
        Unbounded
      )
    ),
    List()
  )

  val schema: WSchema = WSchema(
    pm = PrefixMap.empty,
    shapesMap = Map(Start -> shape)
  )

  test("Match shape".only) {
    val q42 = new ItemIdValueImpl("Q42", "http://www.wikidata.org/entity/")
    val p31 = new PropertyIdValueImpl("P31", "http://www.wikidata.org/entity/")
    val q515 = new ItemIdValueImpl("Q515", "http://www.wikidata.org/entity/")
    val statementBuilder =
      StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515)
    val itemDocument =
      ItemDocumentBuilder.forItemId(q42).withStatement(statementBuilder.build())
    val expected = Matching(List(shape), itemDocument)
    println(s"#### Expected: $expected")
    assertEquals(
      Matcher(wShEx = schema).matchStart(itemDocument.build()),
      expected
    )
  }

  /*  test("Don't match shape when fails value") {
    val q42 = new ItemIdValueImpl("Q42", "http://www.wikidata.org/entity/")
    val p31 = new PropertyIdValueImpl("P31", "http://www.wikidata.org/entity/")
    val q515 = new ItemIdValueImpl("Q516", "http://www.wikidata.org/entity/")
    val statementBuilder =
      StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515)
    val itemDocument =
      ItemDocumentBuilder.forItemId(q42).withStatement(statementBuilder.build())
    assertEquals(Matcher(wShEx = schema).matchStart(itemDocument.build()).matches, false)
  }

  test("Don't match shape when fails property") {
    val q42 = new ItemIdValueImpl("Q42", "http://www.wikidata.org/entity/")
    val p31 = new PropertyIdValueImpl("P32", "http://www.wikidata.org/entity/")
    val q515 = new ItemIdValueImpl("Q515", "http://www.wikidata.org/entity/")
    val statementBuilder =
      StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515)
    val itemDocument =
      ItemDocumentBuilder.forItemId(q42).withStatement(statementBuilder.build())
    assertEquals(Matcher(wShEx = schema).matchStart(itemDocument.build()).matches, false)
  }

  test("Match shape when some value matches") {
    val q42 = new ItemIdValueImpl("Q42", "http://www.wikidata.org/entity/")
    val p31 = new PropertyIdValueImpl("P31", "http://www.wikidata.org/entity/")
    val q515 = new ItemIdValueImpl("Q515", "http://www.wikidata.org/entity/")
    val q516 = new ItemIdValueImpl("Q516", "http://www.wikidata.org/entity/")
    val s1 = StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515).build()
    val s2 = StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q516).build()
    val itemDocument = ItemDocumentBuilder.forItemId(q42).withStatement(s1).withStatement(s2)
    assertEquals(
      Matcher(wShEx = schema).matchStart(itemDocument.build()),
      Matching(List(shape))
    )
  }
   */
}
