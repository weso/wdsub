package es.weso.wdsub
import es.weso.rdf.nodes._
import org.wikidata.wdtk.datamodel.interfaces._
import scala.collection.JavaConverters._
import org.wikidata.wdtk.datamodel.implementation._
import org.slf4j.LoggerFactory

case class Matcher(schema: Schema) {

    lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    /**
      * Match an item document with the shapes in a schema
      *
      * @param itemDocument item document
      * @return list of shapes that match an itemDocument
      */
    def matchSomeShape(itemDocument: ItemDocument): List[ShapeExpr] =
      schema.shapes.filter(matchShape(itemDocument))

    def matchShape(itemDocument: ItemDocument)(shapeExpr: ShapeExpr): Boolean =
      shapeExpr match {
          case Shape(TripleConstraint(predicate, Some(ValueSet(IRIValue(value)::Nil)), None, None)) => 
           matchPredicateValue(predicate, value, itemDocument)
          case _ => false 
        }

    /**
      * Get local name and prefix of a IRI
      * This code has been adapted from WikidataToolkit ItemIdValueImpl
      * @param iri
      * @return a pair with (localName, base)
      */
    def splitIri(iri: IRI): (String, String) = {
        val iriStr = iri.getLexicalForm
        val separator = iriStr.lastIndexOf('/') + 1;
        try {
         (iriStr.substring(separator), iriStr.substring(0, separator))   
        } catch {
            case e: IllegalArgumentException => throw new IllegalArgumentException("Invalid Wikibase entity IRI: " + iriStr, e)
        }
    }

    def matchPredicateValue(predicate: IRI, value: IRI, itemDocument: ItemDocument): Boolean = {
      logger.debug(s"ItemDocument to match: $itemDocument")
      logger.debug(s"Predicate: ${predicate.getLexicalForm}")
      val (localName, base) = splitIri(predicate) 
      val propertyId = new PropertyIdValueImpl(localName, base)
      val statementGroup = itemDocument.findStatementGroup(propertyId)
      if (statementGroup == null) {
          logger.debug(s"No statement group for property: $propertyId")
          false
      }
      else {
        val statements = statementGroup.getStatements().asScala
        logger.debug(s"Some values for predicate matched: ${statements.size}")  
        val matched = statements.filter(matchValueStatement(value))
        println(s"Matched: $matched")
        !matched.isEmpty
      }
    }

    def matchValueStatement(value: IRI)(statement: Statement): Boolean = {
      val statementValue = statement.getClaim().getValue()
      val valueVisitor: ValueVisitor[Boolean] = MatchVisitor(value)
      statementValue.accept(valueVisitor)
    }

 case class MatchVisitor(expectedIri: IRI) extends ValueVisitor[Boolean] {
   val (localName, base) = splitIri(expectedIri) 
   val expectedEntityId = new ItemIdValueImpl(localName,base)

   override def visit(v: EntityIdValue): Boolean = {
     v == expectedEntityId
   }
     
   override def visit(v: GlobeCoordinatesValue): Boolean = false
   override def visit(v: MonolingualTextValue): Boolean = false
   override def visit(v: QuantityValue): Boolean = false
   override def visit(v: StringValue): Boolean = false
   override def visit(v: TimeValue): Boolean = false
   override def visit(v: UnsupportedValue): Boolean = false
 }

}