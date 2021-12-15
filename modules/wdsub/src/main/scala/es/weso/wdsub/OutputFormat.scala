package es.weso.wdsub

sealed trait OutputFormat { val name: String }
case object JsonDump extends OutputFormat { override val name = "JSON" }
case object TurtleDump extends OutputFormat { override val name = "TURTLE" }
