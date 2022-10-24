package es.weso.wdsub.writer

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument

import java.io.OutputStream
import es.weso.wbmodel._
import es.weso.wbmodel.serializer._
import org.eclipse.rdf4j.rio.RDFFormat

case class DumpWriter(os: OutputStream, df: WBSerializeFormat) {

  val serializer: Serializer = df match {
    case WBSerializeFormat.Turtle => RDFSerializer(RDFFormat.TURTLE)
    case WBSerializeFormat.JSON => JSONSerializer()
    case WBSerializeFormat.Plain => PlainSerializer()
  }

  def writeEntity(entityDocument: EntityDocument): Unit = {
    serializer.serializeOutputStream(entityDocument, os)
  }

  def start(): Unit = { 
   serializer.startOutputStream(os)
  }
  def end(): Unit = serializer.endOutputStream(os)

  def close(): Unit = os.close()

}

object DumpWriter {
  def fromOutputStream(os: OutputStream, df: WBSerializeFormat): DumpWriter = DumpWriter(os,df)
}
