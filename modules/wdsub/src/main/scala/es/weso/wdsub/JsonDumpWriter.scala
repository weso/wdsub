package es.weso.wdsub
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument

import java.io.OutputStream
import java.nio.charset.StandardCharsets

case class JsonDumpWriter(outStream: OutputStream) extends DumpWriter {
  
  val JSON_START_LIST: Array[Byte] = "[\n".getBytes(StandardCharsets.UTF_8)
  val JSON_SEP: Array[Byte] = ",\n".getBytes(StandardCharsets.UTF_8)
  val JSON_END_LIST: Array[Byte] = "\n]".getBytes(StandardCharsets.UTF_8)

  var counter: Int = 0

  val mapper = new ObjectMapper()
  mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)

  def writeItem(itemDocument: ItemDocument): Unit = {
    if (counter > 0) outStream.write(JSON_SEP)
    mapper.writeValue(outStream, itemDocument)
    counter += 1
  }

  def start(): Unit = {
    outStream.write(JSON_START_LIST)
  }

  def end(): Unit = {
    outStream.write(JSON_END_LIST)
  }

}