package es.weso.wdshex

import es.weso.wshex._ 

sealed abstract class ParseError(msg: String) extends Product with Serializable
case class ParseException(e: Throwable) extends ParseError(e.getMessage())
case class ConversionError(e: ConvertError) extends ParseError(s"Error converting shEx to WShEx\nError: ${e}")