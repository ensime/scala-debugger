---
weight: 0
---
# Us

The Scala Debugger project encompasses several different subprojects geared
towards providing a Scala abstraction on top of the Java debugger interface.

Described below are the different subprojects contained underneath the
Scala Debugger project.

---

## Scala Debugger API

Contains the core logic and abstractions to use debugging features
programmatically in Scala including setting breakpoints and stepping
through code.

```
val fileName = "file.scala"
val lineNumber = 37

scalaVirtualMachine.getOrCreateBreakpointRequest(
  fileName,
  lineNumber
).foreach(breakpointEvent => {
  val f = breakpointEvent.fileName
  val l = breakpointEvent.lineNumber

  println(s"Reached breakpoint $$f:$$l")
}
```

## Scala Debugger Tool (SDB)

Contains REPL logic to provide an interactive, JDB-like interface using
the Scala Debugger API.

<video
    class="org-scaladebugger-docs-styles-PageStyle-videoCls"
    poster="/videos/examples/sdb.jpg"
    preload="none"
    controls="true"
    loop="true"
    muted="true">
    <source src="/videos/examples/sdb.webm" type="video/webm">
    <source src="/videos/examples/sdb.mp4" type="video/mp4">
    <source src="/videos/examples/sdb.ogv" type="video/ogv">
    <span>Your browser doesn't support HTML video tag.</span>
</video>

## Scala Debugger Language

Contains parser and interpreter logic to provide a programming language
that is used as the command interface for the Scala Debugger Tool.

```
myFunc := func(a, b) {
  a + b
}

result := myFunc 3 9

print("Result is " ++ result)
```

## Scala Debugger sbt Plugin

Contains an implementation of an sbt plugin that runs the interactive
Scala Debugger Tool from within sbt.

<video
    class="org-scaladebugger-docs-styles-PageStyle-videoCls"
    poster="/videos/examples/sbt-plugin.jpg"
    preload="none"
    controls="true"
    loop="true"
    muted="true">
    <source src="/videos/examples/sbt-plugin.webm" type="video/webm">
    <source src="/videos/examples/sbt-plugin.mp4" type="video/mp4">
    <source src="/videos/examples/sbt-plugin.ogv" type="video/ogv">
    <span>Your browser doesn't support HTML video tag.</span>
</video>

## Scala Debugger Docs

Contains a static site generator to build the Scala Debugger website.

```
sbt 'scalaDebuggerDocs/run --generate --serve --allow-unsupported-media-types'
```
