package es.weso.wdsub.wdtk

import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentDumpProcessor

case class ShExProcessor(
    entityDocumentDumpProcessor: EntityDocumentDumpProcessor,
    entityCounter: EntityCounter
) {}
