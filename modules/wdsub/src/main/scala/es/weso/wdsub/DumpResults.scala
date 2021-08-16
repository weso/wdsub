package es.weso.wdsub

case class DumpResults(totalEntities: Int, matchedEntities: Int) {
    def addMatched = this.copy(totalEntities = this.totalEntities + 1, matchedEntities = this.matchedEntities + 1)
    def addEntity = this.copy(totalEntities = this.totalEntities + 1) 
    override def toString=s"Matched/total entities = $matchedEntities/$totalEntities"
}

object DumpResults {
    def initial: DumpResults = DumpResults(0,0)
}

