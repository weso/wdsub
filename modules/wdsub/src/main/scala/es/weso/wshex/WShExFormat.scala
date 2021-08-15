package es.weso.wshex
import es.weso.rdf.nodes._
import org.wikidata.wdtk.datamodel.interfaces._
import scala.collection.JavaConverters._
import org.wikidata.wdtk.datamodel.implementation._
import org.slf4j.LoggerFactory
import org.wikidata.wdtk.datamodel.interfaces._
import java.nio.file.Path
import cats.effect._
import org.wikidata.wdtk.datamodel.helpers.JsonDeserializer
import org.wikidata.wdtk.datamodel.helpers

sealed trait WShExFormat
case object CompactFormat extends WShExFormat 
case object JSONFormat extends WShExFormat

