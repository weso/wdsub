package es.weso.wdsub

import org.wikidata.wdtk.datamodel.interfaces.EntityDocument

import java.io.OutputStream

abstract class DumpWriter {
  val outStream: OutputStream

  def writeEntity(entityDocument: EntityDocument): Unit

  def start(): Unit
  def end(): Unit

  def close(): Unit = outStream.close()

}
