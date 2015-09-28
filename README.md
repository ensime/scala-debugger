Scala Debugger
==============

[![Build Status](https://travis-ci.org/chipsenkbeil/scala-debugger.svg?branch=master)](https://travis-ci.org/rcsenkbeil/scala-debugger)
[![Scaladoc 2.10](https://img.shields.io/badge/Scaladoc-2.10-34B6A8.svg?style=flat)](www.javadoc.io/doc/org.senkbeil/scala-debugger-api_2.10)
[![Scaladoc 2.11](https://img.shields.io/badge/Scaladoc-2.11-34B6A8.svg?style=flat)](www.javadoc.io/doc/org.senkbeil/scala-debugger-api_2.11)
[![Join the chat at https://gitter.im/rcsenkbeil/scala-debugger](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/rcsenkbeil/scala-debugger?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A simple debugger library for Scala.

Installing with SBT
-------------------

Hosted on Maven Central and can be installed via the following:

    libraryDependencies += "org.senkbeil" %% "scala-debugger-api" % "1.0.0"

Potential Development Gotchas
-----------------------------

- When moving from Mac OS X's IntelliJ to the IntelliJ of Linux Mint, the
  tools.jar of OpenJDK/Oracle JDK was not picked up. I needed to manually open
  up the SDK being used and add the tools.jar of 
  `/usr/lib/jvm/java-7-openjdk-amd64/lib/` to the classpath. This allows me to
  use the Sun-based `com.sun.jdi` package for a Java debugger interface rather
  than C++.

- When using the launching debugger, I noticed that it was creating an address
  of senkbeil.org:RANDOMPORT. _senkbeil.org_ corresponded to my host name.
  Attempting to telnet into the address and port provided resulted in a failed
  connection. Switching my hostname to _locahost_ allowed the main process
  (and telnet) to connect and use the JVM process.

Other Development Notes
-----------------------

- After observing IntelliJ's Scala plugin and the Scala IDE, it appears that
  stepping into a frame is not as simple as one would like in Scala. The
  process appears to use a series of hard-coded class and methods names to
  filter out when stepping into the next frame. This is performed using a
  series of step into and out of operations until the next valid location is
  reached.

