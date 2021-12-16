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

As an example, the following command:

```
wdsub dump -s examples/humans.shex -o target/outputFile.json.gz examples/100lines.json.gz
```

processes the dump file `examples/100lines.json` using the ShEx schema `examples/humans.shex` 
 generating the file `target/outputFile.json.gz` 


## Installation and compilation

The tool has been implemented in [Scala](https://www.scala-lang.org/) abd uses  [sbt](https://www.scala-sbt.org/) for compilation. In order to create a standalone binary, you can use:

```
sbt universal:packageBin
```

Once it has been run, the binary will be available as a compressed file at: 

```
target/universal/wdsubroot-version.zip
```

Once that file is uncompressed, the executable script is in folder `bin` and is called `wdsubroot`

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

In order to process dumps from docker, you can run:

```
docker run -d -v [folder-with-dumps]:/data -v [folder-with-schemas]:/shex -v [output-folder]:/dumps wesogroup/wdsub:0.0.9 dump -o /dumps/resultDump.json.gz -s /shex/[shexFile].shex /data/[dumpFile].json.gz
```


## Docs

The documentation of the project is generated with [mdoc](https://scalameta.org/mdoc) and [Docusaurus](https://docusaurus.io/). 

Although the documentation is generated automatically with github actions, you can generate the documentation locally using:

```
> sbt docs/mdoc
> cd website && yarn install && yarn run build
```

## More information

Another tool that creates subsets from wikidata dumps is [WDumper](https://github.com/bennofs/wdumper)

## Publishing to OSS-Sonatype

This project uses [the sbt ci release](https://github.com/olafurpg/sbt-ci-release) plugin for publishing to [OSS Sonatype](https://oss.sonatype.org/).

##### SNAPSHOT Releases
Open a PR and merge it to watch the CI release a -SNAPSHOT version [here](https://oss.sonatype.org/#view-repositories;releases~browsestorage)

##### Full Library Releases
1. Push a tag and watch the CI do a regular release
2. `git tag -a v0.1.0 -m "v0.1.0"`
3. `git push origin v0.1.0`
_Note that the tag version MUST start with v._

## Author & contributors

* Author: [Jose Emilio Labra Gayo](http://labra.weso.es)
