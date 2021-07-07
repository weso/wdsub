package es.weso.wdsub
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher
import cats.effect._
object EntityFetcher {

    lazy val wbdf: WikibaseDataFetcher = WikibaseDataFetcher.getWikidataDataFetcher()

    def fetchEntity(entity: String): IO[Entity] = for {
      entityDocument <- IO { wbdf.getEntityDocument(entity) } 
    } yield Entity(entityDocument)
}