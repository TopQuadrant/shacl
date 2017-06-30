# TopBraid SHACL API

**An open source implementation of the W3C Shapes Constraint Language (SHACL) based on the Jena API.**

Contact: Holger Knublauch (holger@topquadrant.com)

Can be used to perform SHACL constraint checking and rule inferencing in any Jena-based Java application.
This API also serves as a reference implementation developed in parallel to the SHACL spec.
**The code is totally not optimized for performance, just for correctness. And there are unfinished gaps!**

Coverage:
* SHACL Core and SHACL-SPARQL validation
* SHACL Advanced Features (Rules etc)
* SHACL JavaScript Extensions

The same code is used in the TopBraid products (currently aligned with the upcoming TopBraid 5.4 release).
For interoperability with TopBraid, and during the transition period from SPIN to SHACL, this library
uses code from org.topbraid.spin packages. These will eventually be refactored.
Meanwhile, please don't rely on any class from the org.topbraid.spin packages directly.

Feedback and questions should become GitHub issues or sent to TopBraid Users mailing list:
https://groups.google.com/forum/#!forum/topbraid-users
Please prefix your messages with [SHACL API]

To get started, look at the class ValidationUtil in
the package org.topbraid.shacl.validation.
There is also an [Example Test Case](../master/src/test/java/org/topbraid/shacl/ValidationExample.java)
