package es.weso.wshex

import java.io.InputStream
import cats.effect._
import java.nio.file.Path

case class WShEx(schema: Schema, path: Option[Path] = None, format: Option[String] = None) {

    /**
      * Returns the start shape expression if declared, or the first shape expression
      */
    lazy val startShapeExpr: Option[ShapeExpr] = schema.startShapeExpr
  
}

object WShEx {
    def fromPath(path: Path, format: String = "ShExC"): IO[WShEx] = for {
        schema <- es.weso.shex.Schema.fromFile(path.toFile().getAbsolutePath(),format)
        resolvedSchema <- es.weso.shex.ResolvedSchema.resolve(schema, None)
        wshex <- IO.fromEither(ShEx2WShEx.convertSchema(resolvedSchema))
    } yield WShEx(wshex, Some(path), Some(format))

    def unsafeFromPath(path: Path, format: String = "ShExC"): WShEx = {
        import cats.effect.unsafe.implicits.global
        fromPath(path, format).unsafeRunSync()
    }
}