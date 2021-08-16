package es.weso.wshex
import es.weso.rdf.nodes._
import org.wikidata.wdtk.datamodel.interfaces._
import scala.collection.JavaConverters._
import org.wikidata.wdtk.datamodel.implementation._
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.datamodel.interfaces._
import java.nio.file.Path
import cats.effect._
import org.wikidata.wdtk.datamodel.helpers.JsonDeserializer
import org.wikidata.wdtk.datamodel.helpers
import es.weso.utils.internal.CollectionCompat._

/**
  * Matcher contains methods to match a WShEx schema with Wikibase entities
  *
  * @param wShEx schema
  * @param site URL that identifies the site. By default: http://www.wikidata.org/entity/ 
  * @param verbose by default false
  */
case class Matcher(wShEx: WShEx, 
  site: String = "http://www.wikidata.org/entity/", 
  verbose: Boolean = false) {

  private lazy val jsonDeserializer = new helpers.JsonDeserializer(site)
  
  private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

  private def info(msg: String): Unit = if (verbose) logger.info(msg)

  /**
    * Checks if an entityDocument matches the start shape of a WShEx schema
    * If the schema doesn't have a start declaration, it tries to match 
    * the first shape expression declared
    * 
    * @param entityDocument
    * @return a matching report
    */
  def matchStart(entityDocument: EntityDocument): MatchingStatus = {
    wShEx.startShapeExpr match {
      case None => NoMatching(List(NoShapeExprs(wShEx)))
      case Some(se) => matchShapeExpr(entityDocument, se)
    }
  }

  /**
    * Match a JSON string that represents an Entity document against the start shape or the first shape 
    *
    * @param jsonStr
    * @return a matching stsatus
    */
  def matchJsonStart(jsonStr: String): MatchingStatus = {
    val entityDocument = jsonDeserializer.deserializeEntityDocument(jsonStr)
    matchStart(entityDocument)
  }

  def matchShapeExpr(entityDocument: EntityDocument, shapeExpr: ShapeExpr): MatchingStatus =
      shapeExpr match {
          case Shape(id, te) =>
            matchTripleExpr(entityDocument,te, shapeExpr) 
          case sand: ShapeAnd => {
            val ls: LazyList[MatchingStatus] = sand.exprs.toLazyList.map(matchShapeExpr(entityDocument,_))
            MatchingStatus.combineAnds(ls)
          }
          case sor: ShapeOr => {
            val ls: LazyList[MatchingStatus] = sor.exprs.toLazyList.map(matchShapeExpr(entityDocument,_))
            MatchingStatus.combineOrs(ls)
          }
          case snot: ShapeNot => {
            val ms = matchShapeExpr(entityDocument,snot.shapeExpr)
            if (ms.matches) NoMatching(List(NotShapeFail(snot.shapeExpr, entityDocument)))
            else Matching(List(shapeExpr), ms.dependencies)
          }
          case _ => {
            // TODO: Pending
            NoMatching(List(NotImplemented(s"matchShape: $shapeExpr")))
          } 
        }

  private def matchTripleExpr(entityDocument: EntityDocument, te: TripleExpr, se: ShapeExpr): MatchingStatus = te match {
    case TripleConstraint(predicate, Some(ValueSet(_, IRIValue(value)::Nil)), None, None) => 
      matchPredicateValue(predicate, value, entityDocument, se)
    case _ => 
      NoMatching(List(NotImplemented(s"matchTripleExpr: $te")))
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

  private def matchPredicateValue(predicate: IRI, value: IRI, entityDocument: EntityDocument, se: ShapeExpr): MatchingStatus = {
      val (localName, base) = splitIri(predicate) 
      val propertyId = new PropertyIdValueImpl(localName, base)
      entityDocument match {
        case sd: StatementDocument => {
         val statementGroup = sd.findStatementGroup(propertyId)
         if (statementGroup == null) {
          info(s"No statement group for property: $propertyId")
          NoMatching(List(NoStatementGroupProperty(propertyId, entityDocument)))
         } else {
        val statements = statementGroup.getStatements().asScala
        info(s"Statements with predicate $predicate that matched: ${statements}")  
        val matched = statements.filter(matchValueStatement(value))
        info(s"Statements with predicate $predicate that match also value ${value}: $matched")
        if (matched.isEmpty) 
          NoMatching(List(NoStatementMatchesValue(predicate,value,entityDocument)))
        else Matching(List(se))   
       }
       } 
       case _ => {
         NoMatching(List(NoStatementDocument(entityDocument)))
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
  def fromPath(schemaPath: Path, verbose: Boolean, format: WShExFormat = CompactFormat): IO[Matcher] = 
    WShEx.fromPath(schemaPath,format)
    .map(s => Matcher(wShEx = s, verbose = verbose))

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
  def unsafeFromPath(schemaPath: Path, verbose: Boolean = false, format: WShExFormat = CompactFormat): Matcher = {
    import cats.effect.unsafe.implicits.global
    fromPath(schemaPath, verbose, format).unsafeRunSync()
  }  

  def unsafeFromString(str: String, verbose: Boolean = false, format: WShExFormat = CompactFormat): Either[ParseError, Matcher] = {
    WShEx.unsafeFromString(str, format)
    .map(s => Matcher(wShEx = s, verbose = verbose))
  }
}