---
weight: 0
---
# Installation

How to install the Scala debugger CLI.

---

## Prerequisites

As the CLI uses the Scala debugger API, you need to have `tools.jar` available
(provided by Oracle's JDK and OpenJDK). This is typically as simple as
installing the JDK on your system.

At runtime, the Scala debugger CLI will attempt to load `tools.jar` from a
variety of locations including `JDK_HOME`, `JAVA_HOME`, and the system
property `java.home`. If the Scala debugger API has issues loading `tools.jar`,
you should set either `JDK_HOME` or `JAVA_HOME` to the path to your JDK.

See the [Scala debugger API prerequisites][api_prerequisites] section for more
information on finding your JDK path.

## Downloading the binary

The Scala debugger CLI has fat jars hosted on Github, built against Scala 2.10,
2.11, and 2.12. You can find a link to the latest release of the CLI here:

- [sdb 1.1.0-M3 built with Scala 2.10][latest_binary_2.10]
- [sdb 1.1.0-M3 built with Scala 2.11][latest_binary_2.11]
- [sdb 1.1.0-M3 built with Scala 2.12][latest_binary_2.12]

The CLI can debug code independently of the version of Scala that it is built
against; so, pick whichever binary you prefer.

## Verifying it works

1. Download a binary distribution as described in the
   [earlier section][downloading_the_binary]
2. Start another Java program with 
   `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005`
3. Run the CLI binary via `java -jar <YOUR_CLI_BINARY>.jar`
4. Attach to the other Java program via `attach 5005`
5. Issue other commands such as listing the threads of the remote JVM via 
   `threads`

*[JDI]: Java Debugger Interface

[api_prerequisites]: /api/getting-started/installation#prerequisities
[downloading_the_binary]: /sdb/getting-started/installation#downloading-the-binary
[latest_binary_2.10]: https://github.com/ensime/scala-debugger/releases/download/v1.1.0-M3/sdb-1.1.0-M3-2.10.jar
[latest_binary_2.11]: https://github.com/ensime/scala-debugger/releases/download/v1.1.0-M3/sdb-1.1.0-M3-2.11.jar
[latest_binary_2.12]: https://github.com/ensime/scala-debugger/releases/download/v1.1.0-M3/sdb-1.1.0-M3-2.12.jar
