package es.weso.wdsub
import org.wikidata.wdtk.dumpfiles._
import cats.effect._
import cats.implicits._
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path
import es.weso.shex
import java.io.OutputStream
import org.apache.commons.compress.compressors.gzip._
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.io.BufferedOutputStream
import java.nio.charset.StandardCharsets
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.JsonGenerator

case class JsonDumpWriter(outStream: OutputStream) {
  
  val JSON_START_LIST = "[\n".getBytes(StandardCharsets.UTF_8)
  val JSON_SEP = ",\n".getBytes(StandardCharsets.UTF_8)
  val JSON_END_LIST = "\n]".getBytes(StandardCharsets.UTF_8)

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


