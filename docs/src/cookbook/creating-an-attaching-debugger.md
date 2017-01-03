# Creating an Attaching Debugger

---

## Overview

The following is a brief example of using the attaching debugger to connect
to an already-running target JVM process that has been started with proper
JDWP options.

The target JVM is started separately in the example code using
`JDITools.spawn`.

## Code

### AttachingDebuggerExample.scala

```scala
import org.scaladebugger.api.debuggers.AttachingDebugger
import org.scaladebugger.api.utils.JDITools

/**
 * Starts a target JVM process and connects to it using the
 * attaching debugger.
 */
object AttachingDebuggerExample extends App {
  // Get the executing class name (remove $ from object class name)
  val klass = SomeAttachingMainClass.getClass
  val className = klass.name.replaceAllLiterally("$", "")

  // Spawn the target JVM process using our helper function
  val targetJvmProcess = JDITools.spawn(
    className = className,
    port = 5005,
    suspend = true // Wait to start the main class until after connected
  )

  val attachingDebugger = AttachingDebugger(port = 5005)

  attachingDebugger.start { s =>
    println("Attached to JVM: " + s.uniqueId)

    // Shuts down our debugger
    attachingDebugger.stop()

    // Shuts down the remote process
    targetJvmProcess.destroy()
  }

  // Keep the sample program running while our debugger is running
  while (attachingDebugger.isRunning) Thread.sleep(1)
}
```

### SomeAttachingMainClass.scala

```scala
/**
 * Sample main class that does nothing.
 */
object SomeAttachingMainClass {
  def main(args: Array[String]): Unit = {
    // Does nothing
  }
}
```

