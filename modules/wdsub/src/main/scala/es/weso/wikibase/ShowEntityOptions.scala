package es.weso.wikibase

case class ShowEntityOptions(
    maxStatements: Option[Int],
    showAllValues: Boolean
) {
    def witMaxStatements(m: Option[Int]): ShowEntityOptions =
        this.copy(maxStatements = m)
}

object ShowEntityOptions {
    lazy val default: ShowEntityOptions = 
        ShowEntityOptions(
            maxStatements = None, 
            showAllValues = true
            ) 
}
