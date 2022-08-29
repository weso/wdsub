package es.weso.wdsub.writer 

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument

import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.charset.Charset

case class PlainDumpWriter(outStream: OutputStream) extends DumpWriter {

  def writeEntity(entityDocument: EntityDocument): Unit = {
    outStream.write((entityDocument.getEntityId().getId() + "\n").getBytes(Charset.defaultCharset()))
  }

  def start(): Unit = {}

  def end(): Unit = {}

}
