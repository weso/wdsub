package es.weso.wdsub

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

object ShEx2WShEx {


  def convertSchema(shexSchema: shex.AbstractSchema): Either[ConvertError, Schema] = for {
    shapes <- shexSchema.shapes.getOrElse(List()).map(convertShapeExpr).sequence
  } yield Schema(shexSchema.prefixMap, shapes)

  def convertShapeExpr(se: shex.ShapeExpr): Either[ConvertError, ShapeExpr] = se match {
    case nc: shex.NodeConstraint => convertNodeConstraint(nc)
    case s: shex.Shape => convertShape(s)
    case _ => UnsupportedShapeExpr(se).asLeft
  }

  def convertNodeConstraint(nc: shex.NodeConstraint): Either[ConvertError, NodeConstraint] = 
   nc match {
     case shex.NodeConstraint(id, None, None, List(), values, None, None) => convertValueSet(values.getOrElse(List())).map(ValueSet)
     case _ => UnsupportedNodeConstraint(nc).asLeft
   }

  def convertValueSet(values: List[shex.ValueSetValue]): Either[ConvertError, List[ValueSetValue]] =
    values.map(convertValueSetValue).sequence

  def convertValueSetValue(value: shex.ValueSetValue): Either[ConvertError, ValueSetValue] = 
    value match {
      case shex.IRIValue(i) => IRIValue(i).asRight
      case _ => UnsupportedValueSetValue(value).asLeft
    }

  def convertShape(s: shex.Shape): Either[ConvertError,Shape] = s match {
    case shex.Shape(_, None, None, None, Some(expr), None, None, None, None) => 
      convertTripleExpr(expr).map(Shape(_))
    case _ => UnsupportedShape(s).asLeft
  }

  def convertTripleExpr(te: shex.TripleExpr): Either[ConvertError, TripleExpr] = te match {
    case shex.EachOf(_, expressions, None, None, None, None) => 
      expressions.map(convertTripleExpr).sequence.map(EachOf)
    case tc: shex.TripleConstraint => convertTripleConstraint(tc)
  }

  def convertTripleConstraint(tc: shex.TripleConstraint): Either[ConvertError, TripleConstraint] = tc match {
    case shex.TripleConstraint(id, None, None, pred, value, optMin, optMax, None, None, None) => for {
      se <- value match {
        case None => none[ShapeExpr].asRight
        case Some(se) => convertShapeExpr(se).map(s => Some(s))
      } 
    } yield TripleConstraint(pred, se)
  }

}