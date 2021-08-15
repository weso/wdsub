package es.weso.wshex

import es.weso.rdf.nodes._
import es.weso.rdf._

import munit._
import org.wikidata.wdtk.datamodel.implementation._
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder

object IRIHelpers {

 val wde = IRI("http://www.wikidata.org/entity/")

 def p(n:Int): IRI = {
    wde + ("P" + n.toString)
 }

 def q(n:Int): IRI = {
    wde + ("Q" + n.toString)
 }

}

class WShExTest extends FunSuite {
    import IRIHelpers._

    val shape = Shape(None,TripleConstraint(IRI("http://www.wikidata.org/entity/P31"), 
          Some(ValueSet(None,List(IRIValue(IRI("http://www.wikidata.org/entity/Q515")))))))

    val schema: Schema = Schema(
        pm = PrefixMap.empty,
        shapes = List(shape)
    )

    test("Match shape") {
      val q42 = new ItemIdValueImpl("Q42","http://www.wikidata.org/entity/")
      val p31 = new PropertyIdValueImpl("P31", "http://www.wikidata.org/entity/")
      val q515 = new ItemIdValueImpl("Q515","http://www.wikidata.org/entity/")
      val statementBuilder = 
        StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515)
      val itemDocument = 
        ItemDocumentBuilder.forItemId(q42).withStatement(statementBuilder.build())
      assertEquals(
        Matcher(wShEx = WShEx(schema), verbose = true).matchStart(itemDocument.build()), 
        Matching(List(shape))
      )
    }

    test("Don't match shape when fails value") {
      val q42 = new ItemIdValueImpl("Q42","http://www.wikidata.org/entity/")
      val p31 = new PropertyIdValueImpl("P31", "http://www.wikidata.org/entity/")
      val q515 = new ItemIdValueImpl("Q516","http://www.wikidata.org/entity/")
      val statementBuilder = 
        StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515)
      val itemDocument = 
        ItemDocumentBuilder.forItemId(q42).withStatement(statementBuilder.build())
      assertEquals(
        Matcher(wShEx = WShEx(schema), verbose = true).matchStart(itemDocument.build())matches, false)
    }

    test("Don't match shape when fails property") {
      val q42 = new ItemIdValueImpl("Q42","http://www.wikidata.org/entity/")
      val p31 = new PropertyIdValueImpl("P32", "http://www.wikidata.org/entity/")
      val q515 = new ItemIdValueImpl("Q515","http://www.wikidata.org/entity/")
      val statementBuilder = 
        StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515)
      val itemDocument = 
        ItemDocumentBuilder.forItemId(q42).withStatement(statementBuilder.build())
      assertEquals(
        Matcher(wShEx = WShEx(schema), verbose = true).matchStart(itemDocument.build()).matches, false)
    }

    test("Match shape when some value matches") {
      val q42 = new ItemIdValueImpl("Q42","http://www.wikidata.org/entity/")
      val p31 = new PropertyIdValueImpl("P31", "http://www.wikidata.org/entity/")
      val q515 = new ItemIdValueImpl("Q515","http://www.wikidata.org/entity/")
      val q516 = new ItemIdValueImpl("Q516","http://www.wikidata.org/entity/")
      val s1 = StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q515).build()
      val s2 = StatementBuilder.forSubjectAndProperty(q42, p31).withValue(q516).build()
      val itemDocument = ItemDocumentBuilder.forItemId(q42).withStatement(s1).withStatement(s2)
      assertEquals(
        Matcher(wShEx = WShEx(schema), verbose = true).matchStart(itemDocument.build()), 
        Matching(List(shape)))
    }

}