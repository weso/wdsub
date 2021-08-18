package es.weso.wshex

import es.weso.rdf._
import es.weso.rdf.nodes._
import es.weso.shex.Max
import cats.data._

/**
  * Wikibase Shape Expression abstract Syntax
  * This syntax starts as a subset of ShEx abstract syntax
  * 
  * See: https://www.weso.es/shex-s/api/es/weso/shex/index.html
  * 
  * As future work, we will extend this abstract syntax with support to references, qualifiers, ranks, etc.
  */

case class Schema(
 pm: PrefixMap, 
 shapes: List[ShapeExpr]
) {
 lazy val shapesMap: Map[ShapeLabel,ShapeExpr] = {
    shapes.collect{ case s if s.id.isDefined => (s.id.get, s)}.toMap
  }

 /**
   * Get a shape with label 
   *
   * @param lbl
   * @return the shape expression with that label
   */ 
 def getShape(lbl: ShapeLabel): Option[ShapeExpr] =
    shapesMap.get(lbl)

 /**
   * Start shape expression in a schema
   *
   * @return the start shape expression if it has been declared or the first one. None if there are no shape expressions 
   */   
  lazy val startShapeExpr: Option[ShapeExpr] = 
    getShape(Start).orElse(shapes.headOption)   

}

sealed trait ShapeExpr {
 def id: Option[ShapeLabel]
}

case class ShapeAnd(
    id: Option[ShapeLabel], 
    exprs: List[ShapeExpr]
    ) extends ShapeExpr
case class ShapeOr(
    id: Option[ShapeLabel], 
    exprs: List[ShapeExpr]
    ) extends ShapeExpr
case class ShapeNot(
    id: Option[ShapeLabel],
    shapeExpr: ShapeExpr
    ) extends ShapeExpr
case class Shape(
    id: Option[ShapeLabel], 
    expression: TripleExpr
    ) extends ShapeExpr
case class ShapeRef(
    reference: ShapeLabel
) extends ShapeExpr {
    def id = None
}


abstract sealed trait NodeConstraint 
 extends ShapeExpr 
 with Product with Serializable

case class ValueSet(
    id: Option[ShapeLabel], 
    values: List[ValueSetValue]
    ) extends NodeConstraint

sealed trait ValueSetValue 
sealed trait ObjectValue extends ValueSetValue
case class IRIValue(iri: IRI) extends ObjectValue

abstract class TripleExpr

case class EachOf(expressions: List[TripleExpr]) extends TripleExpr
case class TripleConstraint(
 predicate: IRI, 
 valueExpr: Option[ShapeExpr], 
 optMin: Option[Int] = None, 
 optMax: Option[Max] = None
) extends TripleExpr