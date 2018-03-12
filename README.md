# TopBraid SHACL API

**An open source implementation of the W3C Shapes Constraint Language (SHACL) based on Apache Jena.**

Contact: Holger Knublauch (holger@topquadrant.com)

Can be used to perform SHACL constraint checking and rule inferencing in any Jena-based Java application.
This API also serves as a reference implementation developed in parallel to the SHACL spec.
**The code is not really optimized for performance, just for correctness.**

Coverage:
* [SHACL Core and SHACL-SPARQL validation](https://www.w3.org/TR/shacl/)
* [SHACL Advanced Features (Rules etc)](https://www.w3.org/TR/shacl-af/)
* [SHACL JavaScript Extensions](https://www.w3.org/TR/shacl-js/)

See [SHACL-JS](https://github.com/TopQuadrant/shacl-js) for a pure JavaScript implementation.

The same code is used in the TopBraid products (currently aligned with the upcoming TopBraid 5.4 release).
For interoperability with TopBraid this library uses code from org.topbraid.spin packages.
These dependencies have been removed for release 1.1.0 (currently the source snapshot).
Meanwhile, please don't rely on any class from the org.topbraid.spin packages directly.

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

`http://central.maven.org/maven2/org/topbraid/shacl/`

The binary distribution is:

`http://central.maven.org/maven2/org/topbraid/shacl/*VER*/shacl-*VER*-bin.zip`.

Two command line utilities are included: validate (performs constraint validation) and infer (performs SHACL rule inferencing).

To use them, set up your environment similar to https://jena.apache.org/documentation/tools/ (note that the SHACL download includes Jena).

For example, on Windows:

```
SET SHACLROOT=C:\Users\Holger\Desktop\shacl-1.0.0-bin
SET PATH=%PATH%;%SHACLROOT%\bin
```

As another example, for Linux, add to .bashrc these lines:

```
# for shacl
export SHACLROOT=/home/holger/shacl/shacl-1.0.0-bin/shacl-1.0.0/bin
export PATH=$SHACLROOT:$PATH 
```

Both tools take the following parameters, for example:

`shaclvalidate.bat -datafile myfile.ttl -shapesfile myshapes.ttl`

where `-shapesfile` is optional and falls back to using the data graph as shapes graph.

Currently only Turtle (.ttl) files are supported.

The tools print the validation report or the inferences graph to the output screen.
