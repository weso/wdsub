package es.weso.wdsub

import org.wikidata.wdtk.datamodel.interfaces.ItemDocument

import java.io.OutputStream


abstract class DumpWriter {
  val outStream: OutputStream

  def writeItem(itemDocument: ItemDocument): Unit

  def start(): Unit
  def end(): Unit

  def close(): Unit = outStream.close()

}


