package es.weso.wshex

import org.wikidata.wdtk.datamodel.implementation._
import org.wikidata.wdtk.datamodel.interfaces._
import es.weso.rdf.nodes._

sealed abstract class MatchingError(msg: String) extends Product with Serializable
case class Pending(msg:String) extends MatchingError(s"Pending: $msg")
case class NotImplemented(msg: String) extends MatchingError(s"Not Implemented: $msg")
case class NoShapeExprs(wShEx: WShEx) extends MatchingError(s"No shape expressions in schema ${wShEx.schema}")

case class NoStatementGroupProperty(
    property: PropertyIdValueImpl,
    entityDocument: EntityDocument
    ) extends MatchingError(s"No statement group for property $property\nEntity: $entityDocument")

case class NoStatementMatchesValue(
    predicate: IRI, 
    value: IRI, 
    entityDocument: EntityDocument
    ) extends MatchingError(s"No statements matches predicate ${predicate} with value ${value}\nEntity: $entityDocument")    

case class NoStatementDocument(
    entityDocument: EntityDocument
    ) extends MatchingError(s"Entity is not an StatementDocument\nEntity: ${entityDocument}")    

case class NotShapeFail(
    se: ShapeExpr, 
    entityDocument: EntityDocument) extends MatchingError(s"NOT failed because entity matches shapeExpr\nEntity: $entityDocument\nShapeExpr: $se")    