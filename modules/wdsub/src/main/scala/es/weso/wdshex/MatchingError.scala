package es.weso.wdshex

import es.weso.wikibase._
import org.wikidata.wdtk.datamodel.implementation._
import org.wikidata.wdtk.datamodel.interfaces._
import es.weso.rdf.nodes._
import es.weso.wshex._

sealed abstract class MatchingError(msg: String) extends Product with Serializable
case class Pending(
    msg:String
    ) extends MatchingError(s"Pending: $msg")

case class NotImplemented(
    msg: String
    ) extends MatchingError(s"Not Implemented: $msg")

case class NoShapeExprs(
    wShEx: WShEx
    ) extends MatchingError(s"No shape expressions in schema ${wShEx.schema}")

case class NoStatementGroupProperty(
    property: PropertyIdValue,
    entityDocument: EntityDocument
    ) extends MatchingError(s"No statement group for property $property\nEntity: $entityDocument")

case class NoStatementMatchesValue(
    predicate: IRI, 
    value: IRI, 
    entityDocument: EntityDocument
    ) extends MatchingError(s"""|No statements matches predicate ${predicate} with value ${value}
                                |Entity: $entityDocument""".stripMargin)    

case class NoStatementDocument(
    entityDocument: EntityDocument
    ) extends MatchingError(s"""|Entity is not an StatementDocument
                                |Entity: ${entityDocument}""".stripMargin)    

case class NotShapeFail(
    se: ShapeExpr, 
    entity: Entity) extends MatchingError(s"""|NOT failed because entity matches shapeExpr
                                              |Entity: ${entity.show()}
                                              |ShapeExpr: $se
                                              |""".stripMargin)    

case class NoValuesProperty(
    property: IRI,
    entity: Entity) extends MatchingError(s"""|No values for property: ${property}
                                              |Entity ${entity.show()}
                                              |""".stripMargin)        