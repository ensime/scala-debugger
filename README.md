# Scala Debugger

[![Stories in Ready](https://badge.waffle.io/ensime/scala-debugger.svg?label=ready&title=Ready)](http://waffle.io/ensime/scala-debugger)
[![Build Status](https://ci.senkbeil.org/api/badges/ensime/scala-debugger/status.svg)](https://ci.senkbeil.org/ensime/scala-debugger)
[![Build status](https://ci.appveyor.com/api/projects/status/8mcnhcm1jofomg2f/branch/master?svg=true)](https://ci.appveyor.com/project/chipsenkbeil/scala-debugger/branch/master)
[![Scaladex](https://index.scala-lang.org/ensime/scala-debugger/scala-debugger-api/latest.svg?color=orange)](https://index.scala-lang.org/ensime/scala-debugger)
[![Scaladoc 2.10](https://img.shields.io/badge/Scaladoc-2.10-34B6A8.svg?style=flat)](http://www.javadoc.io/doc/org.scala-debugger/scala-debugger-api_2.10)
[![Scaladoc 2.11](https://img.shields.io/badge/Scaladoc-2.11-34B6A8.svg?style=flat)](http://www.javadoc.io/doc/org.scala-debugger/scala-debugger-api_2.11)
[![Scaladoc 2.12](https://img.shields.io/badge/Scaladoc-2.12-34B6A8.svg?style=flat)](http://www.javadoc.io/doc/org.scala-debugger/scala-debugger-api_2.12)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ensime/scala-debugger)

A simple debugger library for Scala. Tested on OpenJDK 7 for Windows and Linux.
- Visit the [main site](https://scala-debugger.org/) for more documentation.
- Check out the [wiki](https://github.com/ensime/scala-debugger/wiki) for the current roadmap.

## Installing with sbt

Hosted on Maven Central and can be installed via the following:

```scala
libraryDependencies += "org.scala-debugger" %% "scala-debugger-api" % "1.1.0-M3"
```

You also need to install the sbt plugin to make the Java Debugger Interface jar
(tools.jar) available on your classpath when you are compiling and testing:

```scala
addSbtPlugin("org.scala-debugger" % "sbt-jdi-tools" % "1.0.0")
```

## Building from source

Make sure that you have some form of _sbt_ installed and available on your path
as it is used to compile the Scala Debugger source modules.

### Compiling all modules

From the root of the project:

```bash
make
```

or

```bash
make build
```

This will build all modules for Scala 2.10, 2.11, and 2.12.

### Assembling executable jars

From the root of the project:

```bash
make assembly
```

This will assemble relevant modules such as `sdb` for Scala 2.10, 2.11, and 2.12.

## Running tests

Make sure that you have [sbt-extras](https://github.com/paulp/sbt-extras)
installed as it will pick up memory and compiler options from `.jvmopts`.

### Unit Tests

From the root of the project:

```bash
make unit-test-all
```

This will run unit tests for Scala 2.10, 2.11, and 2.12.

_Note: Roughly 2.5 GB of RAM is needed to compile the unit tests._

### Integration Tests

From the root of the project:

```bash
make it-test-all
```

This will run integration tests for Scala 2.10, 2.11, and 2.12.

## View project statistics

This requires you to have [cloc](https://github.com/AlDanial/cloc) installed
and available on your path.

```bash
make stats
```

This will calculate statistical information about the project and print it
out to the terminal.

## License

The Scala debugger API is licensed under [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0).

