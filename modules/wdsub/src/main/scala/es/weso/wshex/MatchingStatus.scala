package es.weso.wshex
import es.weso.rdf.nodes.IRI

case class Dependency(node: IRI, ref: ShapeLabel)
abstract class MatchingStatus {
    def matches: Boolean
}

case class NoMatching(
    matchingErrors: List[MatchingError],
    pendingDependencies: List[Dependency] = List()
    ) extends MatchingStatus {
  override def matches: Boolean = false      
}
case class Match(
    se: List[ShapeExpr], 
    dependencies: List[Dependency] = List()
    ) extends MatchingStatus {
 override def matches: Boolean = true
}