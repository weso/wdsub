package es.weso.wdsub
import es.weso.utils.named._

sealed abstract class DumpMode extends Named

object DumpMode {

  case object DumpOnlyMatched extends DumpMode {
    override val name: String = "OnlyMatched"
  }

  case object DumpWholeEntity extends DumpMode {
    override val name: String = "WholeEntity"
  }

  case object DumpOnlyId extends DumpMode {
    override val name: String = "OnlyId"
  }

  lazy val availableModes = List(DumpOnlyMatched, DumpWholeEntity, DumpOnlyId)

}
