package es.weso.wdsubmain

import java.nio.file.Path
import es.weso.wdsub.DumpOptions

case class Dump(
    filePath: Path,
    action: DumpActionOpt,
    outPath: Option[Path],
    opts: DumpOptions,
    processor: Processor
)
