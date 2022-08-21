package es.weso.wdsub.fs2processor

import cats.effect._
import cats.implicits._
import es.weso.wbmodel.EntityDoc
import cats.implicits._
import org.wikidata.wdtk.rdf.{PropertyRegister, RdfSerializer}
import java.io.OutputStream
import org.eclipse.rdf4j.rio.RDFFormat
import org.wikidata.wdtk.rdf.RdfConverter
import org.wikidata.wdtk.rdf.RdfWriter
import org.wikidata.wdtk.datamodel.implementation.SitesImpl
import org.wikidata.wdtk.rdf.PropertyRegister
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument
import java.net.URI
import java.io.ByteArrayOutputStream
import es.weso.rdf.jena.RDFAsJenaModel
import es.weso.rdf.nodes._
import es.weso.rdf.triples._


case class RDFSerializerErrorUnknownEntity(ed: EntityDoc)
    extends RuntimeException(s"Unexpected entitydoc: $ed. Should be item or property")

case class RDFSerializer(format: RDFFormat) extends Serializer {

  def serialize(entityDoc: EntityDoc): IO[String] = {
    RDFAsJenaModel.empty.flatMap(
      _.use(
        rdf =>
          for {
            _   <- rdf.addTriple(RDFTriple(IRI("x"), IRI("a"), StringLiteral(entityDoc.entityDocument.getEntityId().getId())))
            str <- rdf.serialize("TURTLE")
          } yield str
      )
    )
    /*    for {
      out <- IO { new ByteArrayOutputStream() }
      rdfConverter <- IO {
        val propertyRegister = PropertyRegister
          .getWikidataPropertyRegister()
        // .fetchUsingSPARQL(new URI("https://query.wikidata.org/sparql"))
        val rdfWriter = new RdfWriter(format, out)
        rdfWriter.start()
        val sites        = new SitesImpl()
        val rdfConverter = new RdfConverter(rdfWriter, sites, propertyRegister)
        val tasks        = rdfConverter.getTasks()
        println(s"Tasks value: $tasks")
        rdfConverter
      }
      str <- entityDoc.entityDocument match {
        case id: ItemDocument =>
          IO {
            rdfConverter.writeItemDocument(id)
            out.toString()
          }
        case pd: PropertyDocument =>
          IO {
            rdfConverter.writePropertyDocument(pd)
            out.toString()
          }
        case _ => IO.raiseError(RDFSerializerErrorUnknownEntity(entityDoc))
      }
      _ <- IO.println(s"Line: $str")
    } yield str */

  }

  def sep = "\n"
}

object RDFSerializer {

  def makeSerializer(format: RDFFormat): Resource[IO, RDFSerializer] = {
    def acquire: IO[RDFSerializer] = IO {
      RDFSerializer(format)
    }

    def release(r: RDFSerializer): IO[Unit] = {
      ().pure[IO]
    }

    Resource.make(acquire)(release)
  }
}
