package es.weso.wdsub
import org.wikidata.wdtk.dumpfiles._
import cats.effect._
import cats.implicits._
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

object DumpProcessor {

    private lazy val dumpProcessingController: DumpProcessingController = new DumpProcessingController("wdsubDump")
    private lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    private def info(msg: String): IO[Unit] = IO {
        logger.info(msg)
    }

    private def mkShExProcessor(schema: Schema, verbose: Boolean, timeout: Int): Resource[IO, WShExProcessor] = 
       Resource.make(IO { new WShExProcessor(schema, verbose,timeout) })(e => IO { e.close() })

    private def mkDumpFile(fileName: String): Resource[IO, MwLocalDumpFile] = 
       Resource.make(IO { new MwLocalDumpFile(fileName) })(mwFile => IO { mwFile.getDumpFileStream().close() })
    
    /**
      * Dump process
      *
      * @param fileName name of filename to process
      * @param schema ShEx schema
      * @param verbose if true, show info messages
      * @param timeout timeout in seconds or 0 if no timeout should be used
      * @return dump results
      */   
    def dumpProcess(fileName: String, schema: Schema, verbose: Boolean, timeout: Int): IO[DumpResults] = {
       for {
        results <- (mkShExProcessor(schema,verbose,timeout), mkDumpFile(fileName)).tupled.use {
            case (processor, mwDumpFile) => for {
              _ <- IO { LogConfig.configureLogging() }  
              _ <- IO { dumpProcessingController.registerEntityDocumentProcessor(processor, null, true) }
              _ <- info(s"Processing local dump: $fileName")
              _ <- info(s"DateStamp: ${mwDumpFile.getDateStamp()}")
              _ <- info(s"Available?: ${mwDumpFile.isAvailable()}")
              _ <- IO { dumpProcessingController.processDump(mwDumpFile) }
              totalEntities <- processor.getTotalEntities()
              matchedEntities <- processor.getMatchedEntities()
            } yield DumpResults(totalEntities, matchedEntities)
        }
       } yield results
    }
}


