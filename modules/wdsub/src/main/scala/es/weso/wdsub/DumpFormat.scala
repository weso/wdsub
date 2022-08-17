package es.weso.wdsub
import es.weso.utils.named._

sealed abstract class DumpFormat extends Named {
  def sep: String
}

object DumpFormat {

  case object Turtle extends DumpFormat {
    override val name: String = "Turtle"
    override val sep: String  = "\n"
  }

  case object JSON extends DumpFormat {
    override val name: String = "JSON"
    override val sep: String  = ",\n"
  }

  case object Plain extends DumpFormat {
    override val name: String = "Text"
    override val sep: String  = "\n"
  }

  lazy val availableFormats = List(Turtle, JSON, Plain)

}
