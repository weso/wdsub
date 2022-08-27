package es.weso.wbmodel.serializer

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
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup
import org.wikidata.wdtk.datamodel.interfaces.StatementRank
import org.wikidata.wdtk.datamodel.interfaces.Statement
import org.wikidata.wdtk.datamodel.interfaces.SnakVisitor
import org.wikidata.wdtk.datamodel.interfaces.NoValueSnak
import org.wikidata.wdtk.datamodel.interfaces.SomeValueSnak
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak
import org.wikidata.wdtk.datamodel.interfaces.ValueVisitor
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue
import org.wikidata.wdtk.datamodel.interfaces.StringValue
import org.wikidata.wdtk.datamodel.interfaces.TimeValue
import org.wikidata.wdtk.datamodel.interfaces.UnsupportedValue
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import org.wikidata.wdtk.datamodel.interfaces.FormIdValue
import org.wikidata.wdtk.datamodel.interfaces.LexemeIdValue
import org.wikidata.wdtk.rdf.Vocabulary
import org.antlr.v4
import es.weso.wdsub.DumpOptions
import org.wikidata.wdtk.datamodel.interfaces.Claim
import org.wikidata.wdtk.rdf.values.TimeValueConverter

case class RDFSerializerErrorUnknownEntity(ed: EntityDoc)
    extends RuntimeException(s"Unexpected entitydoc: $ed. Should be item or property")
case class RDFSerializerErrorUnknownEntityIdValue(ed: EntityIdValue)
    extends RuntimeException(s"Unexpected entityIdValue: $ed")

case class RDFSerializer(format: RDFFormat) extends Serializer {

  // val logger = Logger

  /* I would prefer to reuse the definitions from Wikidata-toolkit
     https://github.com/Wikidata/Wikidata-Toolkit/blob/master/wdtk-rdf/src/main/java/org/wikidata/wdtk/rdf/AbstractRdfConverter.java
    However, that code is based on OutputStreams which make the code imperative
    so we decided to replicate those definitions using SRDF
   */

  val wd       = IRI("http://www.wikidata.org/entity/")
  val geo      = IRI(Vocabulary.PREFIX_GEO)
  val wdt      = IRI(Vocabulary.PREFIX_PROPERTY_DIRECT)
  val rdf      = IRI(Vocabulary.PREFIX_RDF)
  val p        = IRI(Vocabulary.PREFIX_PROPERTY)
  val ps       = IRI(Vocabulary.PREFIX_PROPERTY_STATEMENT)
  val pq       = IRI(Vocabulary.PREFIX_PROPERTY_QUALIFIER)
  val pr       = IRI(Vocabulary.PREFIX_PROPERTY_REFERENCE)
  val prov     = IRI(Vocabulary.PREFIX_PROV)
  val rdfs     = IRI(Vocabulary.PREFIX_RDFS)
  val wds      = IRI(Vocabulary.PREFIX_WIKIDATA_STATEMENT)
  val skos     = IRI(Vocabulary.PREFIX_SKOS)
  val schema   = IRI(Vocabulary.PREFIX_SCHEMA)
  val wikibase = IRI(Vocabulary.PREFIX_WBONTO)
  val owl      = IRI(Vocabulary.PREFIX_OWL)
  val xsd      = IRI(Vocabulary.PREFIX_XSD)

  val rdfs_label         = rdfs + "label"
  val skos_prefLabel     = skos + "prefLabel"
  val skos_altLabel      = skos + "altLabel"
  val wikibase_Item      = wikibase + "Item"
  val schema_description = schema + "description"
  val xsd_string         = xsd + "string"

  val wikibasePrefixMap = PrefixMap(
    Map(
      Prefix("geo")      -> geo,
      Prefix("p")        -> p,
      Prefix("ps")       -> ps,
      Prefix("pq")       -> pq,
      Prefix("pr")       -> pr,
      Prefix("prov")     -> prov,
      Prefix("rdf")      -> rdf,
      Prefix("rdfs")     -> rdfs,
      Prefix("wds")      -> wds,
      Prefix("skos")     -> skos,
      Prefix("schema")   -> schema,
      Prefix("wd")       -> wd,
      Prefix("wdt")      -> wdt,
      Prefix("wikibase") -> wikibase,
      Prefix("xsd")      -> xsd
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
    ts.toList.map(_._2).foldM(rdf) {
      case (current, v) => current.addTriple(RDFTriple(subj, prop, mkLangString(v)))
    }

  def mkTermsLs(
      subj: IRI,
      prop: IRI,
      ts: java.util.Map[String, java.util.List[MonolingualTextValue]],
      rdf: RDFBuilder
  ): IO[RDFBuilder] = {
    ts.asScala.toList.map(_._2).map(_.asScala).flatten.foldM[IO, RDFBuilder](rdf) {
      case (current, v) => current.addTriple(RDFTriple(subj, prop, mkLangString(v)))
    }
  }

  def mkEntityDocument(e: EntityDocument, rdf: RDFBuilder): IO[RDFBuilder] =
    e match {
      case id: ItemDocument     => mkItemDocument(id, rdf)
      case pd: PropertyDocument => mkPropertyDocument(pd, rdf)
    }

  def mkLabels(td: TermedDocument, rdf: RDFBuilder): IO[RDFBuilder] =
    mkTerms(mkEntity(td), rdfs_label, td.getLabels().asScala.toMap, rdf)

  def mkDescriptions(td: TermedDocument, rdf: RDFBuilder): IO[RDFBuilder] =
    mkTerms(mkEntity(td), schema_description, td.getDescriptions().asScala.toMap, rdf)

  def mkAliases(td: TermedDocument, rdf: RDFBuilder): IO[RDFBuilder] =
    mkTermsLs(mkEntity(td), skos_altLabel, td.getAliases(), rdf)

  def mkDocumentTerms(td: TermedDocument, rdf: RDFBuilder): IO[RDFBuilder] =
    mkLabels(td, rdf) *>
      mkDescriptions(td, rdf) *>
      mkAliases(td, rdf)

  def getBestRank(sg: StatementGroup): Option[StatementRank] = {
    val bestStatements = sg.getBestStatements()
    if (bestStatements != null)
      Some(bestStatements.iterator().next.getRank)
    else
      None
  }

  def isBest(statement: Statement, bestRank: Option[StatementRank]): Boolean = {
    val sr      = statement.getRank()
    val maybeSr = if (sr == null) None else Some(sr)
    bestRank == maybeSr
  }

  def mkSimpleStatement(statement: Statement, rdf: RDFBuilder): IO[RDFBuilder] = {
    val subj             = IRI(statement.getSubject().getIri())
    val pred             = wdt + statement.getMainSnak().getPropertyId().getId()
    val snakRdfConverter = SnakRdfConverter(subj, pred, rdf)
    statement.getMainSnak().accept(snakRdfConverter)
  }

  case class SnakRdfConverter(subj: IRI, pred: IRI, rdf: RDFBuilder)
      extends SnakVisitor[IO[RDFBuilder]]
      with ValueVisitor[IO[RDFBuilder]] {

    override def visit(noValue: NoValueSnak): IO[RDFBuilder] =
      IO.println(s"Not implemented noValue visitor for $subj - $pred") *> rdf.pure[IO]
    override def visit(someValue: SomeValueSnak): IO[RDFBuilder] =
      IO.println(s"Not implemented someValue visitor for $subj - $pred") *> rdf.pure[IO]

    override def visit(value: ValueSnak): IO[RDFBuilder] = {
      val v = value.getValue
      v.accept(this)
    }

    override def visit(value: GlobeCoordinatesValue): IO[RDFBuilder] =
      rdf.addTriple(RDFTriple(subj, pred, GlobeCoordinatesConverter.getLiteral(value)))
    // TODO add complex representation...

    override def visit(value: EntityIdValue): IO[RDFBuilder] = value match {
      case id: ItemIdValue     => rdf.addTriple(RDFTriple(subj, pred, IRI(id.getIri())))
      case id: PropertyIdValue => rdf.addTriple(RDFTriple(subj, pred, IRI(id.getIri())))
      case fd: FormIdValue     => rdf.addTriple(RDFTriple(subj, pred, IRI(fd.getIri())))
      case lv: LexemeIdValue   => rdf.addTriple(RDFTriple(subj, pred, IRI(lv.getIri())))
      case _                   => IO.raiseError(RDFSerializerErrorUnknownEntityIdValue(value))
    }
    override def visit(value: MonolingualTextValue): IO[RDFBuilder] =
      rdf.addTriple(RDFTriple(subj, pred, mkLangString(value)))

    override def visit(value: QuantityValue): IO[RDFBuilder] =
      rdf.addTriple(RDFTriple(subj, pred, QuantityConverter.getQuantityLiteral(value)))

    override def visit(x: StringValue): IO[RDFBuilder] =
      rdf.addTriple(RDFTriple(subj, pred, StringLiteral(x.getString())))

    override def visit(timeValue: TimeValue): IO[RDFBuilder] = {
      val literal = TimeConverter.getTimeLiteral(timeValue)
      rdf.addTriple(RDFTriple(subj, pred, literal))
      // TODO: Add complex representation of time values
    }

    override def visit(x: UnsupportedValue): IO[RDFBuilder] =
      rdf.addTriple(RDFTriple(subj, pred, StringLiteral(x.getTypeJsonString())))

  }

  def mkFullStatement(statement: Statement, rdf: RDFBuilder): IO[RDFBuilder] = {
    val iriStatement = IRI(Vocabulary.getStatementUri(statement))
    val subj         = IRI(statement.getSubject().getIri())
    val propId       = statement.getMainSnak().getPropertyId().getId()
    val pred         = p + propId
    rdf.addTriple(RDFTriple(subj, pred, iriStatement)) *>
      rdf.addType(iriStatement, IRI(Vocabulary.WB_STATEMENT)) *>
      mkClaim(iriStatement, ps + propId, statement.getClaim(), rdf)
    // TODO: references, sitelinks
  }

  def mkClaim(subj: IRI, pred: IRI, claim: Claim, rdf: RDFBuilder): IO[RDFBuilder] = {
    val snakRdfConverter = SnakRdfConverter(subj, pred, rdf)
    claim.getMainSnak().accept(snakRdfConverter) *>
      claim.getAllQualifiers().asScala.toList.foldM(rdf) {
        case (current, snak) => {
          val qualifierConverter = SnakRdfConverter(subj, pq + snak.getPropertyId().getId(), rdf)
          snak.accept(qualifierConverter)
        }
      }
  }

  def mkStatement(statement: Statement, rdf: RDFBuilder, bestRank: Option[StatementRank]): IO[RDFBuilder] = {
    if (isBest(statement, bestRank)) {
      mkSimpleStatement(statement, rdf) *>
        mkFullStatement(statement, rdf)
    } else
      mkFullStatement(statement, rdf)
  }

  def mkStatementGroup(sg: StatementGroup, rdf: RDFBuilder, bestRank: Option[StatementRank]): IO[RDFBuilder] = {
    sg.iterator().asScala.toList.foldM(rdf) {
      case (current, statement) => mkStatement(statement, current, bestRank)
    }
  }

  def mkDocumentStatements(sd: StatementDocument, rdf: RDFBuilder): IO[RDFBuilder] = {
    val statementGroup = sd.getStatementGroups().asScala.toList
    statementGroup.foldM(rdf) {
      case (current, sg) => {
        val bestRank = getBestRank(sg)
        mkStatementGroup(sg, current, bestRank)
      }
    }
  }

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

  def removePrefixes(str: String): String = {
    removePrefix(str) match {
      case None          => str
      case Some(removed) => removePrefixes(removed)
    }
  }

}
