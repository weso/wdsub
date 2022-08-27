package es.weso.wbmodel.serializer

// The following code has been translated from Wikidata-toolkit to Scala
// Original code at: https://github.com/Wikidata/Wikidata-Toolkit/blob/master/wdtk-rdf/src/main/java/org/wikidata/wdtk/rdf/values/GlobeCoordinatesValueConverter.java

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
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue

object GlobeCoordinatesConverter {

  def getLiteral(value: GlobeCoordinatesValue): Literal = {
    val builder: StringBuilder = new StringBuilder();
    if (!value.getGlobe().equals(GlobeCoordinatesValue.GLOBE_EARTH)) {
      builder
        .append("<")
        .append(value.getGlobe().replace(">", "%3E"))
        .append("> ");
    }
    builder.append("Point(");
    builder.append(value.getLongitude());
    builder.append(" ");
    builder.append(value.getLatitude());
    builder.append(")");
    DatatypeLiteral(builder.toString(), IRI(Vocabulary.OGC_LOCATION))
  }

}
