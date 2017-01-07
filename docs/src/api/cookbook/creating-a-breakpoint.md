---
weight: 3
---
# Creating a Breakpoint

---

## Overview

The following demonstrates setting and hitting a breakpoint using the default
profile on `ScalaVirtualMachine`.

## Code

### SingleBreakpointExample.scala

```scala
import org.scaladebugger.api.debuggers.LaunchingDebugger
import org.scaladebugger.api.utils.JDITools

/**
 * Creates a single breakpoint to demonstrate the process.
 */
object SingleBreakpointExample extends App {
  // Get the executing class name (remove $ from object class name)
  val klass = SingleBreakpointMainClass.getClass
  val className = klass.name.replaceAllLiterally("$", "")

  // Add our main class to the classpath used to launch the class
  val classpath = JDITools.jvmClassPath
  val jvmOptions = Seq("-classpath", classpath)

  val launchingDebugger = LaunchingDebugger(
    className = className,
    jvmOptions = jvmOptions,
    suspend = true // Wait to start the main class until after connected
  )

  launchingDebugger.start { s =>
    println("Launched and connected to JVM: " + s.uniqueId)

    // Files are in the form of package/structure/to/class.scala
    val fileName = JDITools.scalaClassStringToFileString(className)
    val lineNumber = 7

    // On reaching a breakpoint for our class below, print out our result
    // and shut down our debugger
    s.onUnsafeBreakpoint(fileName, lineNumber).foreach(e => {
      val path = e.location().sourcePath()
      val line = e.location().lineNumber()

      println(s"Reached breakpoint for $path:$line")
      launchingDebugger.stop()
    })
  }

  // Keep the sample program running while our debugger is running
  while (launchingDebugger.isRunning) Thread.sleep(1)
}
```

### SingleBreakpointMainClass.scala

```scala
// NOTE: Make sure that the line marked as breakpoint is ACTUALLY on line 7

object SingleBreakpointMainClass {
  def main(args: Array[String]): Unit = {
    def noop(): Unit = {}
    while (true) {
      noop() // Breakpoint line is 7
    }
  }
}
```

