package es.weso.wdsub

// This is a "very imperative" entity counter implementation
trait EntityCounter {
  var totalEntities: Int
  var matchedEntities: Int

  def resetCounter(): Unit = {
    totalEntities = 0
    matchedEntities = 0
  }

  def showCounter: String = s"$matchedEntities/$totalEntities"

  def incrementMatched(): Unit = {
    matchedEntities += 1
    totalEntities += 1
  }

  def incrementTotal(): Unit = {
    totalEntities += 1
  }

  def getTotalEntities(): Int   = totalEntities
  def getMatchedEntities(): Int = matchedEntities

}
