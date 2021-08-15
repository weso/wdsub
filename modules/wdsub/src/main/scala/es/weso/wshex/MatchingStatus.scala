package es.weso.wshex

import es.weso.rdf.nodes.IRI
import cats._
import cats.data._
import es.weso.utils.internal.CollectionCompat._

abstract class MatchingStatus {
    def matches: Boolean
    def dependencies: List[Dependency]
    def and(other: => MatchingStatus): MatchingStatus
    def or(other: => MatchingStatus): MatchingStatus
}

case class Matching(
    ses: List[ShapeExpr], 
    override val dependencies: List[Dependency] = List()
    ) extends MatchingStatus {
 override def matches: Boolean = true
 override def and(other: => MatchingStatus): MatchingStatus = other match {
   case m: Matching => Matching(
     ses = this.ses ++ m.ses, 
     this.dependencies ++ m.dependencies
     )
   case nm: NoMatching => nm
 }

 override def or(other: => MatchingStatus): MatchingStatus = this

}

case class NoMatching(
    matchingErrors: List[MatchingError],
    override val dependencies: List[Dependency] = List()
    ) extends MatchingStatus {
  override def matches: Boolean = false      

  override def and(other: => MatchingStatus): MatchingStatus = this
  override def or(other: => MatchingStatus): MatchingStatus = other match {
    case m: Matching => m
    case nm: NoMatching => NoMatching(
      this.matchingErrors ++ nm.matchingErrors, 
      this.dependencies ++ nm.dependencies
    )
  }
}


object MatchingStatus {

  lazy val matchEmpty: MatchingStatus = Matching(ses = List())
  lazy val noMatchingEmpty: MatchingStatus = NoMatching(matchingErrors = List())

  def combineAnds(ls: LazyList[MatchingStatus]): MatchingStatus =
    ls.foldLeft(matchEmpty)(_.and(_))

  def combineOrs(ls: LazyList[MatchingStatus]): MatchingStatus =
    ls.foldLeft(noMatchingEmpty)(_.or(_))

}