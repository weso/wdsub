package es.weso.wdsub.fs2processor

// The following code has been translated from Wikidata-toolkit to Scala
// Original code at: https://github.com/Wikidata/Wikidata-Toolkit/blob/master/wdtk-rdf/src/main/java/org/wikidata/wdtk/rdf/values/TimeValueConverter.java

import java.time.Month
import org.wikidata.wdtk.datamodel.interfaces.TimeValue
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue
import org.wikidata.wdtk.datamodel.interfaces.TimeValue
import org.wikidata.wdtk.rdf.OwlDeclarationBuffer
import org.wikidata.wdtk.rdf.PropertyRegister
import org.wikidata.wdtk.rdf.RdfWriter
import org.wikidata.wdtk.rdf.Vocabulary
import es.weso.rdf.nodes._

object TimeConverter {

  /**
	 * Returns the RDF literal to encode the time component of a given time
	 * value.
	 * <p>
	 * Times with limited precision are encoded using limited-precision XML
	 * Schema datatypes, such as gYear, if available. Wikidata encodes the year
	 * 1BCE as 0000, while XML Schema, even in version 2, does not allow 0000
	 * and interprets -0001 as 1BCE. Thus all negative years must be shifted by
	 * 1, but we only do this if the year is precise.
	 *
	 * @param value
	 *            the value to convert
	 * @param rdfWriter
	 *            the object to use for creating the literal
	 * @return the RDF literal
	 */
  def getTimeLiteral(originalValue: TimeValue): Literal = {

    /* we need to check for year zero before julian date conversion,
		 since that can change the year (if the date is 1 Jan 1 for example)
		*/
    val yearZero: Boolean = originalValue.getYear() == 0

    val gregorian: TimeValue = originalValue.toGregorian();
    val value: TimeValue =
      if (gregorian != null) gregorian
      else originalValue

    val valueYear = value.getYear()

    /* https://www.mediawiki.org/wiki/Wikibase/DataModel/JSON#time says the following about the JSON mapping:

		  The format used for Gregorian and Julian dates use a notation resembling ISO 8601. E.g. “+1994-01-01T00:00:00Z”.
		  The year is represented by at least four digits, zeros are added on the left side as needed.
		  Years BCE are represented as negative numbers, using the historical numbering, in which year 0 is undefined,
		   and the year 1 BCE is represented as -0001, the year 44 BCE is represented as -0044, etc.,
		   like XSD 1.0 (ISO 8601:1988) does.
		  In contrast, the RDF mapping relies on XSD 1.1 (ISO 8601:2004) dates that use the proleptic Gregorian calendar
		  and astronomical year numbering, where the year 1 BCE is represented as +0000 and the year 44 BCE
		  is represented as -0043.
		*/
    // map negative dates from historical numbering to XSD 1.1

    val year: Long = if (valueYear < 0 && value.getPrecision() >= TimeValue.PREC_YEAR) {
      valueYear + 1
    } else valueYear

    val valueMonth = value.getMonth()
    val month: Byte =
      if ((value.getPrecision() < TimeValue.PREC_MONTH || valueMonth == 0) && !yearZero) 1
      else valueMonth

    val valueDay = value.getDay()

    val dayOrOne: Byte =
      if ((value.getPrecision() < TimeValue.PREC_DAY || valueDay == 0) && !yearZero) 1
      else valueDay

    val day = if (value.getPrecision() >= TimeValue.PREC_DAY && !yearZero) {
      val maxDays: Int = if (month > 0 && month < 13) {
        val leap: Boolean = (year % 4L) == 0L && (year % 100L != 0L || year % 400L == 0L)
        Month.of(month).length(leap);
      } else java.lang.Byte.MAX_VALUE

      if (dayOrOne > maxDays) {
        maxDays.asInstanceOf[Byte]
      } else
        dayOrOne
    } else
      valueDay

    val minus: String = if (year < 0) "-" else ""

    val timestamp: String = "%s%04d-%02d-%02dT%02d:%02d:%02dZ".format(
      minus,
      Math.abs(year),
      month,
      day,
      value.getHour(),
      value.getMinute(),
      value.getSecond()
    )

    if (yearZero) {
      StringLiteral("+" + timestamp);
    } else
      DatatypeLiteral(timestamp, IRI(Vocabulary.XSD_DATETIME));
  }

}
