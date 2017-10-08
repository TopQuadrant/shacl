# Processes for the Project

This page describes processes for the SHACL API Project.

----

## Local Build

To build the latest snapshot code for your local system:

Clone the github repo, run maven in the local directory:

```
git clone https://github.com/TopQuadrant/shacl
cd shacl
mvn clean install
```

The latest SNAPSHOT code is now available in you local maven
repository.  Check the pom.xml for the correct version string.

```
<dependency>
  <groupId>org.topbraid</groupId>
  <artifactId>shacl</artifactId>
  <version>VERSION</version>
</dependency>
```

----

## Release

### Prerequisites

One time setup:

1. A PGP key for signing the release. This needs to be registered on one
of the public servers e.g. [http://pgpkeys.mit.edu/](http://pgpkeys.mit.edu/).
1. Write access to the GitHub repo - the release process updates the
versions and creates a tag.
1. Write access to the Sonatype OpenSource Hosting Nexus server for `org.topbraid`.

Put your user name and password for accessing the Sonatype open source
repository in your personal `settings.xml` (e.g. `$HOME/.m2/settings.xml`).

```
 <server>
    <id>ossrh.snapshots</id>
    <username>SONATYPE_USERNAME</username>
    <password>SONATYPE_PASSWORD</password>
  </server>
  <server>
    <id>ossrh.releases</id>
    <username>SONATYPE_USERNAME</username>
    <password>SONATYPE_PASSWORD</password>
  </server>
```

### Setup Release Process

The maven release plugin will move the version from SNAPSHOT to the
release version, and tag the github repository. It will also move to
the next SNAPSHOT version after building the release
(autoVersionSubmodules is set true).

If you have multiple PGP keys, choose the right one:

`export KEY="-Darguments=-Dgpg.keyname=KEY_SIGNATURE"`

(Note the slightly odd value here; the release plugin calls maven
recursively and `-Darguments=` is maven recursively passing
down arguments to the subprocess.)

If you have only one key, set this to the empty string:

```
export KEY=""
```

or omit `$KEY`.

### Dry run

It is advisable to dry run the release:
```
mvn release:clean release:prepare -DdryRun=true $KEY
```

### Check

Look in `target/`

You should see various files built.

It still says "SNAPSHOT" because the dry run does not change the version in POM.

### Do the release

This has two steps:

`mvn release:clean release:prepare`
`mvn release:perform $KEY`

### If it goes wrong:

`mvn release:rollback`
`mvn release:clean`

### Release to central

The steps so far pushed the built artifacts to the staging repository.

To push them up to central.maven, go to https://oss.sonatype.org/

* Find repo (it's open) `orgtopbraid....`
* Check it ("content" tab)
* Close it, at which point the checking rules run.
* Refresh the webpage until rule checkign completes.
* If all is good, click "release" at the top.

### Clearup

Check where any intermediate files are left over.

You may wish to build the new SNAPSHOT development version for your
local system with:

`mvn clean install`

and clean your local maven repository of old snapshots.
