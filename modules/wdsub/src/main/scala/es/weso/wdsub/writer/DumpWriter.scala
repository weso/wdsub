package es.weso.wdsub.writer

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument

import java.io.OutputStream
import es.weso.wdsub.DumpFormat
import es.weso.wdsub.DumpFormat.Turtle
import es.weso.wbmodel.serializer.Serializer
import es.weso.wbmodel.serializer.RDFSerializer
import es.weso.wdsub.DumpFormat.JSON
import es.weso.wbmodel.serializer.JSONSerializer
import es.weso.wbmodel._
import es.weso.wdsub.DumpFormat.Plain
import es.weso.wbmodel.serializer.PlainSerializer
import org.eclipse.rdf4j.rio.RDFFormat

case class DumpWriter(os: OutputStream, df: DumpFormat) {

  val serializer: Serializer = df match {
    case Turtle => RDFSerializer(RDFFormat.TURTLE)
    case JSON => JSONSerializer()
    case Plain => PlainSerializer()
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
  def fromOutputStream(os: OutputStream, df: DumpFormat): DumpWriter = DumpWriter(os,df)
}
