package es.weso.wdsub.fs2processor

import cats.implicits._
import org.wikidata.wdtk.rdf.PropertyRegister
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection
import org.wikidata.wdtk.datamodel.helpers.Datamodel

case class BasicPropertyRegister() 
  extends PropertyRegister("P1921", 
            BasicApiConnection.getWikidataApiConnection(), 
            Datamodel.SITE_WIKIDATA
            ) {

}
