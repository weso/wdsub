package es.weso.wdsub
import cats.implicits._
import fs2._
import fs2.io._
import fs2.compression._
import java.nio.file.Path
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument
import cats.effect._
import java.io.InputStream
import java.io.OutputStream
import fs2.io.file.Files

/**
  * Configuration options for DumpOptions
  *
  * @param chunkSize size of chunks for stream processing
  */
case class DumpOptions(
  chunkSize: Int,
  decompressInput: Boolean,
  compressOutput: Boolean
) {
  def withoutDecompressInput: DumpOptions = this.copy(decompressInput = false)
  def withDecompressInput: DumpOptions = this.copy(decompressInput = true)
  def withoutCompressOutput: DumpOptions = this.copy(compressOutput = false)
  def withCompressOutput: DumpOptions = this.copy(compressOutput = true)
}

object DumpOptions {

  /**
    * Default dump options
    *
    * @return
    */
  def default: DumpOptions = 
    DumpOptions(
      chunkSize = 4096,
      decompressInput = true,
      compressOutput = true
    )
}


