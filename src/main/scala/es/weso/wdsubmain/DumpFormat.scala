package es.weso.wdsubmain
import es.weso.wbmodel.serializer.WBSerializeFormat
import es.weso.utils.named._

import es.weso.utils.named.Named
sealed abstract class DumpFormat extends Named {
  def toWBSerializeFormat: WBSerializeFormat
}

object DumpFormat {
 def availableFormats = List(JSON, Turtle, Plain) 
 case object JSON extends DumpFormat {
   override def name = "JSON"
   override def toWBSerializeFormat: WBSerializeFormat = WBSerializeFormat.JSON
 }
 case object Turtle extends DumpFormat {
  override def name = "TURTLE"
  override def toWBSerializeFormat: WBSerializeFormat = WBSerializeFormat.Turtle
 }
 case object Plain extends DumpFormat {
  override def name = "PLAIN"
  override def toWBSerializeFormat: WBSerializeFormat = WBSerializeFormat.Plain
 }
}
