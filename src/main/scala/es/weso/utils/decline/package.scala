package es.weso.utils
import com.monovore.decline._
import cats._
import cats.implicits._
import cats.data.Validated
import es.weso.utils.named._

package object decline {

  def validatedList[A <: Named](optName: String, ls: List[A]): Opts[A] = {
    val lsStr = ls.map(_.name).mkString(",")
    Opts
      .option[String](optName, help = s"$optName. Possible values: $lsStr")
      .mapValidated(
        str =>
          ls.find(v => v.name.toLowerCase() == str.toLowerCase()) match {
            case None    => Validated.invalidNel(s"Invalid $optName, possible values: $lsStr")
            case Some(p) => Validated.valid(p)
          }
      )
  }
}
