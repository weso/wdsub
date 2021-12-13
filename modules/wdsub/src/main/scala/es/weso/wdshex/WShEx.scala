package es.weso.wdshex

import java.io.InputStream
import cats.effect._
import java.nio.file.Path
import cats.implicits._
import es.weso.wshex._

case class WShEx(schema: Schema, path: Option[Path] = None, format: Option[WShExFormat] = None) {

    /**
      * Returns the start shape expression if declared, or the first shape expression
      */
    lazy val startShapeExpr: Option[ShapeExpr] = schema.startShapeExpr
  
}

object WShEx {

    private def cnvFormat(format: WShExFormat): String = format match {
        case CompactFormat => "ShEXC"
        case JSONFormat => "JSON"
    }

    def unsafeFromString(str: String, format: WShExFormat): Either[ParseError, WShEx] = {
        import cats.effect.unsafe.implicits.global
        try {
          val schema = es.weso.shex.Schema.fromString(str,cnvFormat(format)).unsafeRunSync()
          val wShEx = ShEx2WShEx().convertSchema(schema)
          wShEx.bimap(ConversionError(_), WShEx(_))
        } catch {
            case e: Exception => ParseException(e).asLeft
        }
    }

    def fromPath(path: Path, format: WShExFormat = CompactFormat): IO[WShEx] = for {
        schema <- es.weso.shex.Schema.fromFile(path.toFile().getAbsolutePath(), cnvFormat(format))
        resolvedSchema <- es.weso.shex.ResolvedSchema.resolve(schema, None)
        wshex <- IO.fromEither(ShEx2WShEx().convertSchema(resolvedSchema))
    } yield WShEx(wshex, Some(path), Some(format))

    def unsafeFromPath(path: Path, format: WShExFormat = CompactFormat): WShEx = {
        import cats.effect.unsafe.implicits.global
        fromPath(path, format).unsafeRunSync()
    }
}