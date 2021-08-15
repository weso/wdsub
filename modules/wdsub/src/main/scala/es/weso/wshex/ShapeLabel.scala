package es.weso.wshex
import es.weso.rdf.nodes._

abstract sealed trait ShapeLabel extends Product with Serializable
case object Start extends ShapeLabel 
case class IRILabel(iri: IRI) extends ShapeLabel
case class BNodeLabel(bnode: BNode) extends ShapeLabel

