package es.weso.wdsub

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.rdf4j.rio.RDFWriter
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument

import java.io.OutputStream
import java.nio.charset.StandardCharsets

case class RDFDumpWriter(outStream: OutputStream, rdfwriter: RDFWriter) extends DumpWriter {
  
  var counter: Int = 0

  def writeItem(itemDocument: ItemDocument): Unit = {
/*    if (counter > 0) outStream.write(JSON_SEP)
    mapper.writeValue(outStream, itemDocument) */
    counter += 1
  }

  def start(): Unit = {
//    outStream.write(JSON_START_LIST)
  }

  def end(): Unit = {
//    outStream.write(JSON_END_LIST)
  }

}