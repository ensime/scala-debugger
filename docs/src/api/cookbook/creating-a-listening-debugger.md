# Creating a Listening Debugger

---

## Overview

The following is a brief example of using the listening debugger to receive
connections from target JVM processes.

The target JVM is started separately in the example code using
`JDITools.spawn`.

## Code

### ListeningDebuggerExample.scala

```scala
import org.scaladebugger.api.debuggers.ListeningDebugger
import org.scaladebugger.api.utils.JDITools

/**
 * Starts a target JVM process and connects to it using the
 * listening debugger.
 */
object ListeningDebuggerExample extends App {
  // Get the executing class name (remove $ from object class name)
  val klass = SomeListeningMainClass.getClass
  val className = klass.name.replaceAllLiterally("$", "")

  val listeningDebugger = ListeningDebugger(port = 5005)

  listeningDebugger.start { s =>
    println("Received connection from JVM: " + s.uniqueId)

    // Shuts down our debugger
    listeningDebugger.stop()

    // Shuts down the remote process
    targetJvmProcess.destroy()
  }

  // Spawn the target JVM process using our helper function
  val targetJvmProcess = JDITools.spawn(
    className = className,
    port = 5005,
    server = false, // Don't listen for connections but instead do
                    // the connecting
    suspend = true // Wait to start the main class until after connected
  )

  // Keep the sample program running while our debugger is running
  while (listeningDebugger.isRunning) Thread.sleep(1)
}
```

### SomeListeningMainClass.scala

```scala
/**
 * Sample main class that does nothing.
 */
object SomeListeningMainClass {
  def main(args: Array[String]): Unit = {
    // Does nothing
  }
}
```

