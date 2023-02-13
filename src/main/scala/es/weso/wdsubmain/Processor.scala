package es.weso.wdsubmain
import es.weso.utils.named._

import es.weso.utils.named.Named
sealed trait Processor extends Named

object Processor {
  case object WDTK extends Processor { override val name = "WDTK" }
  case object Fs2  extends Processor { override val name = "Fs2"  }

  val availableProcessors = List(WDTK, Fs2)
}
