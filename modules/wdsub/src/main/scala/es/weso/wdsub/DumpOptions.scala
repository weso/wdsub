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
// import fs2.io.file.Files
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
    showSchema: Boolean,
    dumpMode: DumpMode,
    verbose: Boolean
) {
  val jsonDeserializer = new helpers.JsonDeserializer(site)

  def withChunkSize(n: Int): DumpOptions            = this.copy(chunkSize = n)
  def withDecompressInput(di: Boolean): DumpOptions = this.copy(decompressInput = di)
  def withCompressOutput(co: Boolean): DumpOptions  = this.copy(compressOutput = co)
  def withoutOnlyCount: DumpOptions                 = this.copy(onlyCount = false)
  def withOnlyCount: DumpOptions                    = this.copy(onlyCount = true)
  def withMaxConcurrent(n: Int): DumpOptions        = this.copy(maxConcurrent = n)
  def withSite(site: String): DumpOptions           = this.copy(site = site)
  def withShowCounter(sc: Boolean): DumpOptions     = this.copy(showCounter = sc)
  def withShowSchema(sc: Boolean): DumpOptions      = this.copy(showSchema = sc)
  def withVerbose(v: Boolean): DumpOptions          = this.copy(verbose = v)
  def withDumpMode(dm: DumpMode): DumpOptions       = this.copy(dumpMode = dm)
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
      showSchema = false,
      dumpMode = DumpMode.DumpOnlyMatched,
      verbose = false
    )
}
