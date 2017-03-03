---
weight: 0
---
# Installation

How to install the Scala debugger plugin for sbt.

---

## Prerequisites

As the plugin uses the Scala debugger API, you need to have `tools.jar`
available (provided by Oracle's JDK and OpenJDK). This is typically as
simple as installing the JDK on your system.

At runtime, the Scala debugger CLI will attempt to load `tools.jar` from a
variety of locations including `JDK_HOME`, `JAVA_HOME`, and the system
property `java.home`. If the Scala debugger API has issues loading `tools.jar`,
you should set either `JDK_HOME` or `JAVA_HOME` to the path to your JDK.

See the [Scala debugger API prerequisites][api_prerequisites] section for more
information on finding your JDK path.

## Setting up the plugin

Add the following plugin to your `~/.sbt/0.13/plugins/plugins.sbt` file:

```
addSbtPlugin("org.scala-debugger" % "sbt-scala-debugger" % "1.1.0-M3")
```

## Starting the plugin

From sbt, run the `sdb:run` command.

## Verifying it works

1. Set up the plugin as described in the [earlier section][setting_up_plugin]
2. Start another Java program with 
   `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005`
3. Run the plugin via `sbt sdb:run`
4. Attach to the other Java program via `attach 5005`
5. Issue other commands such as listing the threads of the remote JVM via 
   `threads`

*[JDI]: Java Debugger Interface

[api_prerequisites]: /api/getting-started/installation#prerequisities
[setting_up_plugin]: /sbt-plugin/user-manual/installation#setting-up-the-plugin
