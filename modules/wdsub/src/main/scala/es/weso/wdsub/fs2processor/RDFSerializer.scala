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
import es.weso.rdf.jena._
import es.weso.rdf._
import es.weso.rdf.nodes._
import es.weso.rdf.triples._
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument
import org.wikidata.wdtk.datamodel.interfaces.TermedDocument
import org.wikidata.wdtk.datamodel.interfaces.StatementDocument
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue
import scala.jdk.CollectionConverters._

case class RDFSerializerErrorUnknownEntity(ed: EntityDoc)
    extends RuntimeException(s"Unexpected entitydoc: $ed. Should be item or property")

case class RDFSerializer(format: RDFFormat) extends Serializer {

  /* I would prefer to reuse the definitions from Wikidata-toolkit
     https://github.com/Wikidata/Wikidata-Toolkit/blob/master/wdtk-rdf/src/main/java/org/wikidata/wdtk/rdf/AbstractRdfConverter.java
    However, that code is based on OutputStreams which make the code imperative
    so we decided to replicate those definitions using SRDF
   */

  val wdt_IRI      = IRI("http://www.wikidata.org/prop/direct/")
  val rdf_IRI      = IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#")
  val wd_IRI       = IRI("http://www.wikidata.org/entity/")
  val p_IRI        = IRI("http://www.wikidata.org/prop/")
  val ps_IRI       = IRI("http://www.wikidata.org/prop/statement/")
  val pq_IRI       = IRI("http://www.wikidata.org/prop/qualifier/")
  val pr_IRI       = IRI("http://www.wikidata.org/prop/reference/")
  val rdfs_IRI     = IRI("http://www.w3.org/2000/01/rdf-schema#")
  val skos_IRI     = IRI("http://www.w3.org/2004/02/skos/core#")
  val wikibase_IRI = IRI("http://wikiba.se/ontology#")

  val rdfs_label     = rdfs_IRI + "label"
  val skos_prefLabel = skos_IRI + "prefLabel"
  val skos_altLabel  = skos_IRI + "altLabel"
  val wikibase_Item  = wikibase_IRI + "Item"

  val wikibasePrefixMap = PrefixMap(
    Map(
      Prefix("wd")       -> wd_IRI,
      Prefix("wdt")      -> wdt_IRI,
      Prefix("p")        -> p_IRI,
      Prefix("ps")       -> ps_IRI,
      Prefix("pq")       -> pq_IRI,
      Prefix("pr")       -> pr_IRI,
      Prefix("wikibase") -> wikibase_IRI,
      Prefix("rdf")      -> rdf_IRI,
      Prefix("rdfs")     -> rdfs_IRI,
      Prefix("skos")     -> skos_IRI
    )
  )

  def prefixDecls: IO[String] =
    RDFAsJenaModel.empty.flatMap(
      _.use(
        rdf =>
          for {
            _   <- rdf.addPrefixMap(wikibasePrefixMap)
            str <- rdf.serialize("TURTLE")
          } yield str
      )
    )

  def mkLangString(str: MonolingualTextValue): LangLiteral = LangLiteral(str.getText(), Lang(str.getLanguageCode()))

  def mkTerms(subj: IRI, prop: IRI, ts: Map[String, MonolingualTextValue], rdf: RDFBuilder): IO[RDFBuilder] =
    ts.toList.map(_._2).foldRight(rdf.pure[IO]) {
      case (v, current) => current.flatMap(_.addTriple(RDFTriple(subj, prop, mkLangString(v))))
    }

  def mkEntityDocument(e: EntityDocument, rdf: RDFBuilder): IO[RDFBuilder] =
    e match {
      case id: ItemDocument     => mkItemDocument(id, rdf)
      case pd: PropertyDocument => mkPropertyDocument(pd, rdf)
    }

  def mkLabels(td: TermedDocument, rdf: RDFBuilder): IO[RDFBuilder] =
    mkTerms(mkEntity(td), rdfs_label, td.getLabels().asScala.toMap, rdf)

  def mkDocumentTerms(td: TermedDocument, rdf: RDFBuilder): IO[RDFBuilder] =
    mkLabels(td, rdf) /* *>
    mkDescription(td) *>
    mkAliases(td)
   */

  def mkDocumentStatements(sd: StatementDocument, rdf: RDFBuilder): IO[RDFBuilder] =
    rdf.pure[IO]

  def mkItemDocument(id: ItemDocument, rdf: RDFBuilder): IO[RDFBuilder] =
    rdf.addType(mkEntity(id), wikibase_Item) *>
      mkDocumentTerms(id, rdf) *>
      mkDocumentStatements(id, rdf)

  def mkPropertyDocument(pd: PropertyDocument, rdf: RDFBuilder): IO[RDFBuilder] =
    rdf.addType(mkEntity(pd), wikibase_Item)

  /*    addTriple(
      RDFTriple(mkEntity(id) , IRI("a"), StringLiteral(e.getEntityId().getId()))
    ) */

  def mkEntity(e: EntityDocument): IRI =
    IRI(e.getEntityId().getIri)

  def serialize(entityDoc: EntityDoc): IO[String] = {
    RDFAsJenaModel.empty.flatMap(
      _.use(
        rdf =>
          for {
            _   <- rdf.addPrefixMap(wikibasePrefixMap)
            _   <- mkEntityDocument(entityDoc.entityDocument, rdf)
            str <- rdf.serialize("TURTLE")
          } yield RDFSerializer.removePrefixes(str)
      )
    )
  }

  def start = prefixDecls

  def end = IO("")
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

  private def removeLine(str: String): String =
    str.substring(str.indexOf("\n") + 1)

  private def removePrefix(str: String): Option[String] = {
    if (str.startsWith("prefix") || str.startsWith("@prefix")) Some(removeLine(str))
    else None
  }

  private def removePrefixes(str: String): String = {
    removePrefix(str) match {
      case None          => str
      case Some(removed) => removePrefixes(removed)
    }
  }

}
