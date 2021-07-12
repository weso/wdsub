# wdsub
This project is a Wikibase Subsetting tool based on [Shape Expressions(ShEx)](http://www.shex.io).

The project processes wikidata dumps and extracts a subset based on a Shape Expression.

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

## Installation and compilation

The tool has been implemented in [Scala](https://www.scala-lang.org/) abd uses  [sbt](https://www.scala-sbt.org/) for compilation. In order to create a standalone binary, you can use:

```
sbt packageBin
```

### Publish docker image

If you want to create a docker local image, you can run:

```
sbt docker:publishLocal
```

In order to create a docker image (it requires the right credentials):

```
sbt docker:publish
```

The docker image is published as [wesogroup/wdsub](https://hub.docker.com/repository/docker/wesogroup/wdsub)

## Docs

The documentation of the project is generated with [mdoc](https://scalameta.org/mdoc) and [Docusaurus](https://docusaurus.io/). 

Although the documentation is generated automatically with github actions, you can generate the documentation locally using:

```
> sbt docs/mdoc
> cd website && yarn install && yarn run build
```

## More information

Another tool that creates subsets from wikidata dumps is [WDumper](https://github.com/bennofs/wdumper)

## Author & contributors

* Author: [Jose Emilio Labra Gayo](http://labra.weso.es)
