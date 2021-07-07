# wdsub
Wikidata Subsetting tool

## Usage

Command line options:

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


## Building

The tool has been implemented in [Scala](https://www.scala-lang.org/). In order to build from source it is required to have [sbt](https://www.scala-sbt.org/) and run:

```
sbt packageBin
```
