# Installation

How to install the Scala debugger API.

---

## Prerequisites

In order to use the Scala debugger API, you need to have `tools.jar` available
(provided by Oracle's JDK and OpenJDK). This is typically as simple as
installing the JDK on your system.

At runtime, the Scala debugger API will attempt to load `tools.jar` from a
variety of locations including `JDK_HOME`, `JAVA_HOME`, and the system
property `java.home`. If the Scala debugger API has issues loading `tools.jar`,
you should set either `JD_HOME` or `JAVA_HOME` to the path to your JDK.

Mac OS X users can locate their active JDK version using
`/usr/libexec/java_home`.

![Image of directory with tools.js on Mac OS X](/img/getting-started/macosx_tools_jar.png)

Linux users can locate their active JDK using ``ls -l `which javac` `` to
locate the directory containing the compiler and looking one directory up.

![Image of directory with tools.js on Linux](/img/getting-started/linux_tools_jar.png)

## Installing the library

The Scala debugger library is available on Maven Central, which means that you
can install it using sbt:

```scala
libraryDependencies += "org.scala-debugger" %% "scala-debugger-api" % "1.0.0"
```

To be able to compile code using the JDI, you need to also add this plugin to
your `project/plugins.sbt` file:

```scala
addSbtPlugin("org.scala-debugger" % "sbt-jdi-tools" % "1.0.0")
```

The Scala debugger API is currently available for Scala 2.10 and Scala 2.11.

## Verifying it works

1. Create a new project using the Scala debugger API (see
   [installing the library](#installing-the-library) for setting it up).
2. Use `JDITools.isJdiAvailable()` to determine if JDI classes are available
   or on your path via _tools.jar_.
3. Use `JDITools.tryLoadJdi()` to load the JDI classes into your system
   classloader.

See the sample below for a working example:

```scala
import org.scaladebugger.api.utils.JDITools

object VerifyLibrary extends App {
  // Checks if the JDI is available in your runtime
  // classloader or by extracting it from tools.jar
  // found in JDK_HOME or JAVA_HOME
  println("JDI is available: " + JDITools.isJdiAvailable())

  // Loads the JDI from tools.jar and attempts to
  // add it to your system classloader
  println("Loaded JDI: " + JDITools.tryLoadJdi())
}
```

*[JDI]: Java Debugger Interface

