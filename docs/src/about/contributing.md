---
weight: 2
---
# Contributing to the Scala Debugger

## Overview

All contributions are welcome! As a reminder, project is licensed under the
[Apache 2.0 license][license].

1. [Fork the repository][fork].

2. Clone your forked version of the repository.

        git clone git@github.com:your-username/scala-debugger.git

3. Add your code and associated tests. All code-related changes are expected
   to either add new tests or update existing ones.

4. Validate that your changes pass the existing tests along with your own.

        sbt test it:test

5. If relevant (new features, modifying existing features), please update
   the documentation in `docs/`.

6. Push your fork and [submit a pull request][pull_request].

## Developer Notes

### Potential Gotchas

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

[license]: http://choosealicense.com/licenses/apache-2.0/
[fork]: https://github.com/ensime/scala-debugger#fork-destination-box
[pull_request]: https://github.com/ensime/scala-debugger/compare/

