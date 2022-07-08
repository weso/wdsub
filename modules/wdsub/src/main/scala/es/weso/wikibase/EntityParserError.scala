package es.weso.wikkibasedel

sealed abstract trait EntityParserError
case class ParserError(e: Throwable) extends EntityParserError
case object End                      extends EntityParserError
