package org.scaladebugger.repl.backend.functions
import acyclic.file

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.repl.backend.StateManager

/**
 * Represents a collection of functions for managing methods.
 *
 * @param stateManager The manager whose state to share among functions
 */
class MethodFunctions(private val stateManager: StateManager) {
  /** Entrypoint for creating a method entry break. */
  def createEntry(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val methodName = m.get("method").map(_.toString).getOrElse(
      throw new RuntimeException("Missing method argument!")
    )

    jvms.foreach(s => {
      s.onUnsafeMethodEntry(className, methodName, NoResume).foreach(e => {
        val t = e.thread()

        println(s"Method entry hit for $className.$methodName")
      })
    })
  }

  /** Entrypoint for creating a method exit break. */
  def createExit(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val methodName = m.get("method").map(_.toString).getOrElse(
      throw new RuntimeException("Missing method argument!")
    )

    jvms.foreach(s => {
      s.onUnsafeMethodExit(className, methodName, NoResume).foreach(e => {
        val t = e.thread()

        println(s"Method exit hit for $className.$methodName")
      })
    })
  }

  /** Entrypoint for listing all method entry requests. */
  def listEntries(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    jvms.foreach(s => {
      println(s"<= JVM ${s.uniqueId} =>")
      s.methodEntryRequests
        .map(m => s"${m.className}.${m.methodName}")
        .foreach(println)
    })
  }

  /** Entrypoint for listing all method exit requests. */
  def listExits(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    jvms.foreach(s => {
      println(s"<= JVM ${s.uniqueId} =>")
      s.methodExitRequests
        .map(m => s"${m.className}.${m.methodName}")
        .foreach(println)
    })
  }

  /** Entrypoint for clearing a method entry request. */
  def clearEntry(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val methodName = m.get("method").map(_.toString).getOrElse(
      throw new RuntimeException("Missing method argument!")
    )

    import org.scaladebugger.api.profiles.Constants.CloseRemoveAll
    jvms.foreach(s => {
      s.methodEntryRequests
        .filter(m => m.className == className && m.methodName == methodName)
        .map(m => s.onUnsafeMethodEntry(m.className, m.methodName))
        .foreach(_.close(data = CloseRemoveAll))
    })
  }

  /** Entrypoint for clearing a method exit request. */
  def clearExit(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val methodName = m.get("method").map(_.toString).getOrElse(
      throw new RuntimeException("Missing method argument!")
    )

    import org.scaladebugger.api.profiles.Constants.CloseRemoveAll
    jvms.foreach(s => {
      s.methodExitRequests
        .filter(m => m.className == className && m.methodName == methodName)
        .map(m => s.onUnsafeMethodEntry(m.className, m.methodName))
        .foreach(_.close(data = CloseRemoveAll))
    })
  }
}
