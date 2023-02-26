# wdsub
This project is a Wikibase Subsetting tool based on [Shape Expressions (ShEx)](http://www.shex.io).

The project processes Wikidata dumps and extracts a subset based on a Shape Expression.

## Usage as a command line tool

If you have a binary executable `wdsub`, it's usage is similar to linux command line tools. The tool has the following options:


```
Usage:
  wdsub extract
  wdsub dump
Wikidata subsetting command line tool
Options and flags:
 --help
     Display this help text.
 --version, -v
     Print the version number and exit.
Subcommands:
  extract
    Show information about an entity.
  dump
    Process dump files
```

As an example, the following command:

```
wdsub dump -s examples/humans.shex -o target/outputFile.json examples/100lines.json.gz
```

processes the dump file `examples/100lines.json` using the ShEx schema `examples/humans.shex` 
 generating the file `target/outputFile.json` 

The dump options are:

```shell
 Usage:
     wdsub dump --count [--out <file>] [--verbose] [--showCounter] [--compressOutput <string>] [--showSchema] [--dumpMode <string>] [--dumpFormat <string>] [--processor <string>] <dumpFile>
     wdsub dump --show [--maxStatements <integer>] [--out <file>] [--verbose] [--showCounter] [--compressOutput <string>] [--showSchema] [--dumpMode <string>] [--dumpFormat <string>] [--processor <string>] <dumpFile>
     wdsub dump --schema <file> [--schemaFormat <string>] [--verbose <string>] [--out <file>] [--verbose] [--showCounter] [--compressOutput <string>] [--showSchema] [--dumpMode <string>] [--dumpFormat <string>] [--processor <string>] <dumpFile>
 Process example dump file.
 Options and flags:
     --help
         Display this help text.
     --count
         count entities
     --show
         show entities
     --maxStatements <integer>
         max statements to show
     --schema <file>, -s <file>
         ShEx schema
     --schemaFormat <string>
         schemaFormat. Possible values: WShExC,ShExC
     --verbose <string>, -v <string>
         verbose level (0-nothing,1-basic,2-info,3-details,4-debug,5-step,6-all)
     --out <file>, -o <file>
         output path
     --verbose
         verbose mode
     --showCounter
         show counter at the end of process
     --compressOutput <string>
         compress output. Possible values: true,false
     --showSchema
         show schema
     --dumpMode <string>
         dumpMode. Possible values: OnlyMatched,WholeEntity,OnlyId
     --dumpFormat <string>
         dumpFormat. Possible values: Turtle,JSON,Text
     --processor <string>
         processor. Possible values: WDTK,Fs2
```

## Usage from docker

The docker image is published as [wesogroup/wdsub](https://hub.docker.com/repository/docker/wesogroup/wdsub).

In order to process dumps from docker, you can run:

```
docker run -d -v [folder-with-dumps]:/data -v [folder-with-schemas]:/shex -v [output-folder]:/dumps wesogroup/wdsub:{version} dump -o /dumps/resultDump.json -s /shex/[shexFile].shex /data/[dumpFile].json.gz
```


## Building and compiling

### Prerequisites: Install Scala

The tool has been implemented in [Scala](https://www.scala-lang.org/) and uses [sbt](https://www.scala-sbt.org/) for compilation. In order to create a standalone binary, you first need to install sbt. 

Install instructions Scala:
* Linux: https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html

### Clone this repository
Once Scala is installed, clone this repository from GitHub.
```
git clone https://github.com/weso/wdsub.git
``` 

### Go to the cloned directory
``` 
cd wdsub
``` 

### Compilation to local binary

```
sbt universal:packageBin
```

Once it has been run, the binary will be available as a compressed file at: 

```
target/universal/wdsubroot-version.zip
```

Once that file is uncompressed, the executable script is in folder `bin` and is called `wdsubroot`.

### Publish docker image

If you want to create a docker local image, you can run:

```
sbt docker:publishLocal
```

In order to create a docker image (it requires the right credentials):

```
sbt docker:publish
```

## More information

Another tool that creates subsets from wikidata dumps is [WDumper](https://github.com/bennofs/wdumper).

## Author & contributors

* Author: [Jose Emilio Labra Gayo](http://labra.weso.es)

