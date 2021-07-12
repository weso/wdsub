---
id: commandline
title: Command line usage
---

# Command line usage

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

## Examples

Using wdsub with docker using some local dump can be done as:

```
docker run -v [local_folder]:/data wdsubroot:0.0.1 dump -f /data/[dump_file_name.json.gz]
```


