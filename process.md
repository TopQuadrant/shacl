# Processes for the Project

This page describes processes for the SHACL API Project.

----

## Local Build

To build the latest snapshot code for your local system:

Clone the GitHub repo, run maven in the local directory:

```
git clone https://github.com/TopQuadrant/shacl
cd shacl
./mvnw clean install
```

The latest SNAPSHOT code is now available in you local maven
repository. Check the pom.xml for the correct version string.

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
2. Write access to the GitHub repo - the release process updates the
   versions and creates a tag.
3. Write access to the Sonatype OpenSource Hosting Nexus server for `org.topbraid`.

Put your username and password for accessing the Sonatype open source
repository in your personal `settings.xml` (e.g. `$HOME/.m2/settings.xml`).

```
 <server>
    <id>central</id>
    <username>SONATYPE_USERNAME</username>
    <password>SONATYPE_PASSWORD</password>
  </server>
  <server>
    <id>central</id>
    <username>SONATYPE_USERNAME</username>
    <password>SONATYPE_PASSWORD</password>
  </server>
```

### Setup Release Process

#### Versions

Choose the release version, next version and the tag, otherwise maven
will ask for these interactively:

```
export VER="-DreleaseVersion=x.y.z -DdevelopmentVersion=x.next.z-SNAPSHOT -Dtag=shacl-x.y.z"
```

The maven release plugin will move the version from the current SNAPSHOT
to the release version, and tag the GitHub repository. It will also move
to the next SNAPSHOT version after building the release.

#### Signing

If you have multiple PGP keys, choose the right one:

`export KEY="-Darguments=-Dgpg.keyname=KEY_SIGNATURE"`

(Note the slightly odd value here; the release plugin calls maven
recursively and `-Darguments=` is maven recursively passing
down arguments to the subprocess.)

If you have only one key, set this to the empty string:

```
export KEY=""
```

or omit `$KEY` in the examples below.

### Dry run

It is advisable to dry run the release:

```
./mvnw release:clean release:prepare -DdryRun=true -Prelease $VER $KEY
```

### Check

Look in `target/`.

You should see various files built including the binary ".bin.zip", the
javadoc (which is only done in the release cycle), `sources`, `tests`
and `test-sources`. The files should have been signed and `.asc` files
created.

If there is no javadoc jar or no `.asc` files, check you gave the
`-Prelease` argument.

It still says "SNAPSHOT" because the dry run does not change the version in POM.

### Do the release

This has two steps:

`./mvnw release:clean release:prepare`

`./mvnw release:perform -DperformRelease=true`

### If it goes wrong:

`./mvnw release:rollback`  
`./mvnw release:clean`

You may also need to delete the tag.

`git tag -d shacl-x.y.z`  
`git push --delete origin shacl-x.y.z`

### Github

Do a GitHub release using the tag `shacl-x.y.z` above.

### Cleanup

Check where any intermediate files are left over.

You may wish to build the new SNAPSHOT development version for your
local system with:

`./mvnw clean install`

and clean your local maven repository of old snapshots.
