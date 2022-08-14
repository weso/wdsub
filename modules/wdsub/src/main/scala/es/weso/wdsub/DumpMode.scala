package es.weso.wdsub


trait Named {
    def name: String
}


sealed abstract class DumpMode extends Named 

object DumpMode {

 case object DumpOnlyMatched extends DumpMode {
  override val name: String = "OnlyMatched"
 }

 case object DumpWholeEntity extends DumpMode {
  override val name: String = "WholeEntity"
 }

 lazy val availableModes = List(DumpOnlyMatched, DumpWholeEntity)
}

