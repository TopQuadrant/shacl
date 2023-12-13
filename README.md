# TopBraid SHACL API

**An open source implementation of the W3C Shapes Constraint Language (SHACL) based on Apache Jena.**

Contact: Holger Knublauch (holger@topquadrant.com)

Can be used to perform SHACL constraint checking and rule inferencing in any Jena-based Java application.
This API also serves as a reference implementation of the SHACL spec.

Coverage:
* [SHACL Core and SHACL-SPARQL validation](https://www.w3.org/TR/shacl/)
* [SHACL Advanced Features (Rules etc)](https://www.w3.org/TR/shacl-af/)

Former Coverage until version 1.4.0
* [SHACL Compact Syntax](https://w3c.github.io/shacl/shacl-compact-syntax/)

Former Coverage until version 1.3.2
* [SHACL JavaScript Extensions](https://www.w3.org/TR/shacl-js/)

The TopBraid SHACL API is internally used by the European Commission's generic [SHACL-based RDF validator](https://www.itb.ec.europa.eu/shacl/any/upload) (used to validate RDF content against SHACL shapes)
and [SHACL shape validator](https://www.itb.ec.europa.eu/shacl/shacl/upload) (used to validate SHACL shapes themselves).

The same code is used in the TopBraid products (currently aligned with the TopBraid 7.1 release).

Feedback and questions should become GitHub issues or sent to TopBraid Users mailing list:
https://groups.google.com/forum/#!forum/topbraid-users
Please prefix your messages with [SHACL API]

To get started, look at the class ValidationUtil in
the package org.topbraid.shacl.validation.
There is also an [Example Test Case](../master/src/test/java/org/topbraid/shacl/ValidationExample.java)

## Application dependency

Releases are available in the central maven repository:

```
<dependency>
  <groupId>org.topbraid</groupId>
  <artifactId>shacl</artifactId>
  <version>*VER*</version>
</dependency>
```

## Command Line Usage

Download the latest release from:

`https://repo1.maven.org/maven2/org/topbraid/shacl/`

The binary distribution is:

`https://repo1.maven.org/maven2/org/topbraid/shacl/*VER*/shacl-*VER*-bin.zip`.

Two command line utilities are included: shaclvalidate (performs constraint validation) and shaclinfer (performs SHACL rule inferencing).

To use them, set up your environment similar to https://jena.apache.org/documentation/tools/ (note that the SHACL download includes Jena).

For example, on Windows:

```
SET SHACLROOT=C:\Users\Holger\Desktop\shacl-1.4.2-bin
SET PATH=%PATH%;%SHACLROOT%\bin
```

As another example, for Linux, add to .bashrc these lines:

```
# for shacl
export SHACLROOT=/home/holger/shacl/shacl-1.4.2-bin/shacl-1.4.2/bin
export PATH=$SHACLROOT:$PATH 
```

Both tools take the following parameters, for example:

`shaclvalidate.bat -datafile myfile.ttl -shapesfile myshapes.ttl`

where `-shapesfile` is optional and falls back to using the data graph as shapes graph.
Add -validateShapes in case you want to include the metashapes (from the tosh namespace in particular).

For the shaclinfer tool, you can use the `-maxiterations` argument to apply SHACL rule inferencing multiple times; this will add inferred results back to the data graph to see if further triples can be inferred.
The tool will iterate until either (a) the maximum number of iterations is reached, or (b) no new triples are inferred. The flag is optional and defaults to `1` (single iteration).

Currently only Turtle (.ttl) files are supported.

The tools print the validation report or the inferences graph to the output screen.

## Dockerfile Usage

The `Dockerfile` in the `.docker` folder includes a minimal Java Runtime Environment for the SHACL API that clocks in at ~85Mb. To get the latest release of the image use:

```
docker build -f .docker/Dockerfile -t ghcr.io/topquadrant/shacl:1.4.2 --build-arg VERSION=1.4.2 .
```
> :warning: It is generally better to use a fixed version of the docker image, rather than the `latest` tag. Consult the package page to find what versions are available.

To use the Docker image, there are two possible commands. To run the validator:

```
docker run --rm -v /path/to/data:/data ghcr.io/topquadrant/shacl:1.4.2 validate -datafile /data/myfile.ttl -shapesfile /data/myshapes.ttl
```

To run rule inferencing:

```
docker run --rm -v /path/to/data:/data ghcr.io/topquadrant/shacl:1.4.2 infer -datafile /data/myfile.ttl -shapesfile /data/myshapes.ttl
```

Any other command after `ghcr.io/topquadrant/shacl:1.4.2` will print the following help page:

```
Please use this docker image as follows:
docker run -v /path/to/data:/data ghcr.io/topquadrant/shacl:1.4.2 [COMMAND] [PARAMETERS]
COMMAND:
	validate 
		to run validation
	infer
		to run rule inferencing
PARAMETERS:
	-datafile /data/myfile.ttl [MANDATORY]
		input to be validated (only .ttl format supported)
	-shapesfile /data/myshapes.ttl [OPTIONAL]
		shapes for validation (only .ttl format supported)
```

If you'd like to build the image locally in an `x86` architecture, use:

```
docker build -f .docker/Dockerfile -t ghcr.io/topquadrant/shacl:1.4.2 --build-arg VERSION=1.4.2 --build-arg ARCH_BASE=eclipse-temurin:11-alpine .
```

If your architecture is `arm`, use:

```
docker build -f .docker/Dockerfile -t ghcr.io/topquadrant/shacl:1.4.2 --build-arg VERSION=1.4.2 --build-arg ARCH_BASE=amd64/eclipse-temurin:11-alpine .
```
