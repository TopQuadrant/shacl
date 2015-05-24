# TopBraid SHACL API

An open source implementation of the evolving W3C Shapes Constraint Language (SHACL) based on the Jena API

Contact: Holger Knublauch (holger@topquadrant.com)

This can serve as a reference implementation developed in parallel to the SHACL spec.
Can be used to perform SHACL constraint checking in any Jena-based Java application.

The same code is used in the TopBraid products - which is also why we use this particular Jena version.
For interoperability with TopBraid, and during the transition period from SPIN to SHACL, this library
uses code from org.topbraid.spin packages. These will eventually be refactored.
Meanwhile, please don't rely on any class from the org.topbraid.spin packages directly. 

Feedback and questions should go to TopBraid Users mailing list:
https://groups.google.com/forum/#!forum/topbraid-users
Please prefix your messages with [SHACL API]
