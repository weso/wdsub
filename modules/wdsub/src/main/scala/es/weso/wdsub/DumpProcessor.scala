package es.weso.wdsub
import org.wikidata.wdtk.dumpfiles._
import cats.effect._
import cats.implicits._
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

case class DumpResults(totalEntities: Int, matchedEntities: Int)

object DumpProcessor {

    lazy val dumpProcessingController: DumpProcessingController = new DumpProcessingController("wdsubDump")
    lazy val logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    def logInfo(msg: String): IO[Unit] = IO {
        logger.info(msg)
    }

    def mkEntityTimeProcessor: Resource[IO, EntityTimerProcessor] = 
       Resource.make(IO { new EntityTimerProcessor(0) })(e => IO { e.close() })

    def mkDumpFile(fileName: String): Resource[IO, MwLocalDumpFile] = 
       Resource.make(IO { new MwLocalDumpFile(fileName) })(mwFile => IO { mwFile.getDumpFileStream().close() })
    
    def dumpProcess(fileName: String, schema: Schema, verbose: Boolean): IO[DumpResults] = {
       val processor = new WShExProcessor(schema, verbose)
       for {
        results <- (mkEntityTimeProcessor, mkDumpFile(fileName)).tupled.use {
            case (entityTimerProcessor, mwDumpFile) => for {
              _ <- IO { LogConfig.configureLogging() }  
              _ <- IO { dumpProcessingController.registerEntityDocumentProcessor(processor, null, true) }
              _ <- logInfo(s"Processing local dump: $fileName")
              _ <- logInfo(s"DateStamp: ${mwDumpFile.getDateStamp()}")
              _ <- logInfo(s"Available?: ${mwDumpFile.isAvailable()}")
              _ <- IO { dumpProcessingController.processDump(mwDumpFile) }

            } yield DumpResults(processor.getTotalEntities(), processor.getMatchedEntities())
        }
       } yield results
    }
}


