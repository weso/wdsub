package es.weso.wdshex

import java.io.InputStream
import cats.effect._
import java.nio.file.Path
import cats.implicits._
import es.weso.wshex._
import es.weso.utils.VerboseLevel

case class WShEx(schema: WSchema, path: Option[Path] = None, format: Option[WShExFormat] = None) {

  /**
    * Returns the start shape expression if declared, or the first shape expression
    */
  lazy val startShapeExpr: Option[ShapeExpr] = schema.startShapeExpr

}

object WShEx {

  private def cnvFormat(format: WShExFormat): String = format match {
    case WShExFormat.CompactWShExFormat => "ShEXC"
    case WShExFormat.JsonWShExFormat    => "JSON"
  }

  def unsafeFromString(str: String, format: WShExFormat): Either[ParseError, WShEx] = {
    import cats.effect.unsafe.implicits.global
    try {
      val schema = es.weso.shex.Schema.fromString(str, cnvFormat(format)).unsafeRunSync()
      val wShEx  = ShEx2WShEx().convertSchema(schema)
      wShEx.bimap(ConversionError.apply, WShEx(_))
    } catch {
      case e: Exception => ParseException(e).asLeft
    }
  }

  def fromPath(
      path: Path,
      format: WShExFormat = WShExFormat.CompactWShExFormat,
      verboseLevel: VerboseLevel
  ): IO[WShEx] =
    for {
      schema         <- es.weso.shex.Schema.fromFile(path.toFile.getAbsolutePath, cnvFormat(format))
      resolvedSchema <- es.weso.shex.ResolvedSchema.resolve(schema, None, verboseLevel)
      wshex          <- IO.fromEither(ShEx2WShEx().convertSchema(resolvedSchema))
    } yield WShEx(wshex, Some(path), Some(format))

  def unsafeFromPath(
      path: Path,
      format: WShExFormat = WShExFormat.CompactWShExFormat,
      verboseLevel: VerboseLevel
  ): WShEx = {
    import cats.effect.unsafe.implicits.global
    fromPath(path, format, verboseLevel).unsafeRunSync()
  }
}
