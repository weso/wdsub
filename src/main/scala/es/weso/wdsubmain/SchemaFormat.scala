package es.weso.wdsubmain
import es.weso.utils.named._
import es.weso.wshex.WShExFormat

import es.weso.utils.named.Named
sealed trait SchemaFormat extends Named {
  val wshexFormat: WShExFormat
}
object SchemaFormat {
  case object WShExC extends SchemaFormat {
    override val name        = "WShExC"
    override val wshexFormat = WShExFormat.CompactWShExFormat
  }

  case object ES_ShExC extends SchemaFormat {
    override val name        = "ShExC"
    override val wshexFormat = WShExFormat.ESCompactFormat
  }
  val availableSchemaFormats = List(WShExC, ES_ShExC)

}
