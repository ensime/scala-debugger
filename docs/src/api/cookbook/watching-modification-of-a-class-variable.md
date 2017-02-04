---
weight: 5
---
# Watching Modification of a Class Variable

---

## Overview

The following demonstrates watching and receiving a notification when a
variable is modified. This is using the default profile in
`ScalaVirtualMachine`.

## Code

### SingleWatchpointExample.scala

```scala
import org.scaladebugger.api.debuggers.LaunchingDebugger
import org.scaladebugger.api.utils.JDITools

/**
 * Creates a single step to demonstrate the process.
 */
object SingleWatchpointExample extends App {
  // Get the executing class name (remove $ from object class name)
  val klass = SingleWatchpointMainClass.getClass
  val className = klass.getName.replaceAllLiterally("$", "")

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

    val otherClassName = classOf[SingleWatchpointOtherClass].getName
    val fieldName = "x"

    println(s"Watching $otherClassName.$fieldName")
    s.getOrCreateModificationWatchpointRequest(otherClassName, fieldName).foreach(e => {
      val className = e.field.declaringTypeInfo.name
      val fieldName = e.field.name
      val newValue = e.currentValue.toLocalValue

      println(s"$className field $fieldName was updated to $newValue")
      launchingDebugger.stop()
    })
  }

  // Keep the sample program running while our debugger is running
  while (launchingDebugger.isRunning) Thread.sleep(1)
}
```

### SingleWatchpointMainClass.scala

```scala
object SingleWatchpointMainClass {
  def main(args: Array[String]): Unit = {
    val otherClass = new SingleWatchpointOtherClass

    while (true) {
      otherClass.x += 1
    }
  }
}

class SingleWatchpointOtherClass {
  var x: Int = 0
}
```
