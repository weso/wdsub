package es.weso.wdsub
import es.weso.rdf._
import es.weso.rdf.nodes._
import es.weso.shex.Max


/**
  * Wikibase Shape Expression abstract Syntax
  * This syntax starts as a subset of ShEx abstract syntax
  * 
  * See: https://www.weso.es/shex-s/api/es/weso/shex/index.html
  */

case class Schema(
    pm: PrefixMap, 
    shapes: List[ShapeExpr]
    )

sealed trait ShapeExpr

// case class ShapeAnd(exprs: List[ShapeExpr]) extends ShapeExpr
// case class ShapeOr(exprs: List[ShapeExpr]) extends ShapeExpr
case class Shape(expression: TripleExpr) extends ShapeExpr
sealed trait NodeConstraint extends ShapeExpr

case class ValueSet(values: List[ValueSetValue]) extends NodeConstraint

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