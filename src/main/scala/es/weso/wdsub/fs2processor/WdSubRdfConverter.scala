package es.weso.wdsub.fs2processor

import cats.implicits._
import org.wikidata.wdtk.rdf.PropertyRegister
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.rdf.AbstractRdfConverter
import org.wikidata.wdtk.rdf.RdfWriter
import org.wikidata.wdtk.datamodel.interfaces.Sites

/* class WdSubRdfConverter(writer: RdfWriter, sites: Sites, propertyRegister: PropertyRegister)
  extends AbstractRdfConverter(writer,sites, propertyRegister) {

} */

case class WdSubRdfConverter()
