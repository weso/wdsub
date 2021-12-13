package es.weso.wdshex

import es.weso.rdf.nodes.IRI
import es.weso.wshex._

sealed abstract trait Dependency extends Product with Serializable {
 val node: IRI
 val ref: ShapeLabel
}
case class PosDependency(
    override val node: IRI, 
    override val ref: ShapeLabel
    ) extends Dependency

case class NegDependency(
    override val node: IRI, 
    override val ref: ShapeLabel
    ) extends Dependency
