package es.weso.wikkibase

sealed abstract trait EntityParserError
case class  ParserError(e: Throwable) extends EntityParserError
case object End extends EntityParserError
