package es.weso.utils
import com.monovore.decline._
import cats._
import cats.implicits._
import cats.data.Validated
import es.weso.utils.named._

import es.weso.utils.named.Named
package object decline {

  def validatedList[A <: Named](optName: String, ls: List[A], default: Option[A]): Opts[A] = {
    val lsStr = ls.map(_.name).mkString(",")
    val os = Opts
      .option[String](optName, help = s"$optName. Possible values: $lsStr")
      .mapValidated(
        str =>
          ls.find(v => v.name.toLowerCase() == str.toLowerCase()) match {
            case None    => Validated.invalidNel(s"Invalid $optName, possible values: $lsStr")
            case Some(p) => Validated.valid(p)
          }
      )
    // if default is specified, we add it  
    default.fold(os)(v => os.withDefault(v))  
  }
}
