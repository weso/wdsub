package es.weso.wdsub.fs2processor

// The following code has been translated from Wikidata-toolkit to Scala
// Original code at: https://github.com/Wikidata/Wikidata-Toolkit/blob/master/wdtk-rdf/src/main/java/org/wikidata/wdtk/rdf/values/QuantityValueConverter.java

import org.wikidata.wdtk.datamodel.interfaces.TimeValue
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import org.wikidata.wdtk.datamodel.interfaces.TimeValue
import org.wikidata.wdtk.rdf.OwlDeclarationBuffer
import org.wikidata.wdtk.rdf.PropertyRegister
import org.wikidata.wdtk.rdf.RdfWriter
import org.wikidata.wdtk.rdf.Vocabulary
import es.weso.rdf.nodes._
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue

object QuantityConverter {

  def getQuantityLiteral(value: QuantityValue): Literal = {
    DecimalLiteral(value.getNumericValue())
  }

}
