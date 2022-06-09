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
import com.fasterxml.jackson.databind.JsonDeserializer
import org.wikidata.wdtk.datamodel.helpers

/**
  * Configuration options for DumpOptions
  *
  * @param chunkSize size of chunks for stream processing
  */
case class DumpOptions(
    chunkSize: Int,
    decompressInput: Boolean,
    compressOutput: Boolean,
    onlyCount: Boolean,
    maxConcurrent: Int,
    site: String,
    showCounter: Boolean,
    verbose: Boolean
) {
  val jsonDeserializer = new helpers.JsonDeserializer(site)

  def withChunkSize(n: Int): DumpOptions        = this.copy(chunkSize = n)
  def withoutDecompressInput: DumpOptions       = this.copy(decompressInput = false)
  def withDecompressInput: DumpOptions          = this.copy(decompressInput = true)
  def withoutCompressOutput: DumpOptions        = this.copy(compressOutput = false)
  def withCompressOutput: DumpOptions           = this.copy(compressOutput = true)
  def withoutOnlyCount: DumpOptions             = this.copy(onlyCount = false)
  def withOnlyCount: DumpOptions                = this.copy(onlyCount = true)
  def withMaxConcurrent(n: Int): DumpOptions    = this.copy(maxConcurrent = n)
  def withSite(site: String): DumpOptions       = this.copy(site = site)
  def withShowCounter(sc: Boolean): DumpOptions = this.copy(showCounter = sc)
  def withVerbose(v: Boolean): DumpOptions      = this.copy(verbose = v)
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
      compressOutput = true,
      onlyCount = true,
      maxConcurrent = 200,
      site = "http://www.wikidata.org/entity/",
      showCounter = true,
      verbose = false
    )
}
