package es.weso.wshex
import es.weso.rdf.nodes._
import org.wikidata.wdtk.datamodel.interfaces._
import scala.collection.JavaConverters._
import org.wikidata.wdtk.datamodel.implementation._
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument
import java.nio.file.Path
import cats.effect._

/**
  * Matcher contains methods to match a WShEx schema with Wikibase entities
  *
  * @param schema
  * @param verbose
  */
case class Matcher(wShEx: WShEx, verbose: Boolean = false) {

  private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

  private def info(msg: String): Unit = if (verbose) logger.info(msg)

    /**
      * Match an item document with the shapes in a schema
      *
      * @param itemDocument item document
      * @return list of shapes that match an itemDocument
      */
  def matchSomeShape(entityDocument: EntityDocument): List[ShapeExpr] =
      wShEx.schema.shapes.filter(matchShape(entityDocument))

  def matchShape(entityDocument: EntityDocument)(shapeExpr: ShapeExpr): Boolean =
      shapeExpr match {
          case Shape(TripleConstraint(predicate, Some(ValueSet(IRIValue(value)::Nil)), None, None)) => 
           matchPredicateValue(predicate, value, entityDocument)
          case _ => {
            // TODO: Pending
            false
          } 
        }

    /**
      * Get local name and prefix of a IRI
      * This code has been adapted from WikidataToolkit ItemIdValueImpl
      * @param iri
      * @return a pair with (localName, base)
      */
  private def splitIri(iri: IRI): (String, String) = {
        val iriStr = iri.getLexicalForm
        val separator = iriStr.lastIndexOf('/') + 1;
        try {
         (iriStr.substring(separator), iriStr.substring(0, separator))   
        } catch {
            case e: IllegalArgumentException => throw new IllegalArgumentException("Invalid Wikibase entity IRI: " + iriStr, e)
        }
    }

  private def matchPredicateValue(predicate: IRI, value: IRI, entityDocument: EntityDocument): Boolean = {
      val (localName, base) = splitIri(predicate) 
      val propertyId = new PropertyIdValueImpl(localName, base)
      entityDocument match {
        case sd: StatementDocument => {
         val statementGroup = sd.findStatementGroup(propertyId)
         if (statementGroup == null) {
          info(s"No statement group for property: $propertyId")
          false
         } else {
        val statements = statementGroup.getStatements().asScala
        info(s"Statements with predicate $predicate that matched: ${statements}")  
        val matched = statements.filter(matchValueStatement(value))
        info(s"Statements with predicate $predicate that match also value ${value}: $matched")
        !matched.isEmpty
       }
       } 
       case _ => {
         // Should we do something special here?
         false
       }
      }
    }

  private def matchValueStatement(value: IRI)(statement: Statement): Boolean = {
      val statementValue = statement.getClaim().getValue()
      val valueVisitor: ValueVisitor[Boolean] = MatchVisitor(value)
      if (statementValue == null) {
        false
      } else statementValue.accept(valueVisitor)
    }

  private case class MatchVisitor(expectedIri: IRI) extends ValueVisitor[Boolean] {
   val (localName, base) = splitIri(expectedIri) 
   
   private val expectedEntityId = new ItemIdValueImpl(localName,base)

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

object Matcher {

  /**
    * Read a WShEx from a path
    *
    * @param schemaPath: Path where the WShEx schema can be found
    * @param verbose: Print more messages during validation
    * @param format: WShEx format 
    * @return an IO action that returns a matcher
    */  
  def fromPath(schemaPath: Path, verbose: Boolean, format: String = "SHEXC"): IO[Matcher] = 
    WShEx.fromPath(schemaPath,format).map(Matcher(_, verbose))

  /**
    * Read a WShEx from a path
    * This is the synchronous and unsafe version
    * An IO-based version is also available
    *
    * @param schemaPath: Path where the WShEx schema can be found
    * @param verbose: Print more messages during validation
    * @param format: WShEx format 
    * @return a matcher
    */  
  def unsafeFromPath(schemaPath: Path, verbose: Boolean = false, format: String = "SHEXC"): Matcher = {
    import cats.effect.unsafe.implicits.global
    fromPath(schemaPath, verbose, format).unsafeRunSync()
  }  
}