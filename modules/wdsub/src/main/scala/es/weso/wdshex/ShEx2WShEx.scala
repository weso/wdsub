package es.weso.wdshex

import es.weso.rdf._
import es.weso.rdf.nodes._
import es.weso._
import cats.implicits._

sealed trait ConvertError extends Throwable
case class UnsupportedShapeExpr(se: shex.ShapeExpr) extends ConvertError
case class UnsupportedShape(s: shex.Shape) extends ConvertError
case class UnsupportedNodeConstraint(nc: shex.NodeConstraint) extends ConvertError
case class UnsupportedValueSetValue(v: shex.ValueSetValue) extends ConvertError
case class UnsupportedTripleConstraint(tc: shex.TripleConstraint) extends ConvertError

protected object ShEx2WShEx {

  /**
   * Convert a ShEx schema to a WShEx
   **/
  def convertSchema(shexSchema: shex.AbstractSchema): Either[ConvertError, Schema] = for {
    shapes <- shexSchema.shapes.getOrElse(List()).map(convertShapeExpr).sequence
  } yield Schema(shexSchema.prefixMap, shapes)

  private def convertShapeExpr(se: shex.ShapeExpr): Either[ConvertError, ShapeExpr] = se match {
    case nc: shex.NodeConstraint => convertNodeConstraint(nc)
    case s: shex.Shape => convertShape(s)
    case sand: shex.ShapeAnd => for {
      ss <- sand.shapeExprs.map(convertShapeExpr(_)).sequence 
    } yield ShapeAnd(id = convertId(sand.id), exprs = ss)
    case sor: shex.ShapeOr => for {
      ss <- sor.shapeExprs.map(convertShapeExpr(_)).sequence 
    } yield ShapeOr(id = convertId(sor.id), exprs = ss) 
    case snot: shex.ShapeNot =>  
      convertShapeExpr(snot.shapeExpr)
      .map(se => ShapeNot(id = convertId(snot.id), shapeExpr = se))
    case sref: shex.ShapeRef => 
      ShapeRef(reference = convertShapeLabel(sref.reference)).asRight  
    case _ => UnsupportedShapeExpr(se).asLeft
  }

  private def convertId(id: Option[shex.ShapeLabel]): Option[ShapeLabel] =
    id.map(convertShapeLabel)

  private def convertShapeLabel(label: shex.ShapeLabel): ShapeLabel =
    label match {
      case shex.IRILabel(iri) => IRILabel(iri)
      case shex.BNodeLabel(bnode) => BNodeLabel(bnode)
      case shex.Start => Start
    }

  private def convertNodeConstraint(nc: shex.NodeConstraint): Either[ConvertError, NodeConstraint] = 
   nc match {
     case shex.NodeConstraint(id, None, None, List(), Some(values), None, None) => 
      convertValueSet(convertId(id), values)
      // convertValueSet(values.getOrElse(List())).map(ValueSet(id, _))
     case _ => UnsupportedNodeConstraint(nc).asLeft
   }

  private def convertValueSet(id: Option[ShapeLabel], values: List[shex.ValueSetValue]): Either[ConvertError, ValueSet] = 
    convertValueSetValues(values).map(vs => ValueSet(id, vs))

  private def convertValueSetValues(
    values: List[shex.ValueSetValue]
    ): Either[ConvertError, List[ValueSetValue]] =
    values.map(convertValueSetValue).sequence

  private def convertValueSetValue(value: shex.ValueSetValue): Either[ConvertError, ValueSetValue] = 
    value match {
      case shex.IRIValue(i) => IRIValue(i).asRight
      case _ => UnsupportedValueSetValue(value).asLeft
    }

  private def convertShape(s: shex.Shape): Either[ConvertError,Shape] = s match {
    case shex.Shape(id, None, None, None, Some(expr), None, None, None, None) => 
      convertTripleExpr(expr).map(te => Shape(id = convertId(id),expression = te))
    case _ => UnsupportedShape(s).asLeft
  }

  private def convertTripleExpr(te: shex.TripleExpr): Either[ConvertError, TripleExpr] = te match {
    case shex.EachOf(_, expressions, None, None, None, None) => 
      expressions.map(convertTripleExpr).sequence.map(EachOf)
    case tc: shex.TripleConstraint => convertTripleConstraint(tc)
  }

  private def convertTripleConstraint(tc: shex.TripleConstraint): Either[ConvertError, TripleConstraint] = tc match {
    case shex.TripleConstraint(id, None, None, pred, value, optMin, optMax, None, None, None) => for {
      se <- value match {
        case None => none[ShapeExpr].asRight
        case Some(se) => convertShapeExpr(se).map(s => Some(s))
      } 
    } yield TripleConstraint(pred, se)
  }

}