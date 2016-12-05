package org.scaladebugger.tool.backend.functions
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.tool.backend.StateManager

/**
 * Represents a collection of functions for managing methods.
 *
 * @param stateManager The manager whose state to share among functions
 * @param writeLine Used to write output to the terminal
 */
class MethodFunctions(
  private val stateManager: StateManager,
  private val writeLine: String => Unit
) {
  /** Entrypoint for creating a method entry break. */
  def createEntry(m: Map[String, Any]) = {
    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val methodName = m.get("method").map(_.toString).getOrElse(
      throw new RuntimeException("Missing method argument!")
    )

    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    writeLine(s"Set method entry for class $className and method $methodName")
    jvms.foreach(s => {
      val m = s.getOrCreateMethodEntryRequest(className, methodName, NoResume)
      m.foreach(_ => writeLine(s"Method entry hit for $className.$methodName"))
    })
  }

  /** Entrypoint for creating a method exit break. */
  def createExit(m: Map[String, Any]) = {
    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val methodName = m.get("method").map(_.toString).getOrElse(
      throw new RuntimeException("Missing method argument!")
    )

    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    writeLine(s"Set method exit for class $className and method $methodName")
    jvms.foreach(s => {
      val m = s.getOrCreateMethodExitRequest(className, methodName, NoResume)
      m.foreach(_ => writeLine(s"Method exit hit for $className.$methodName"))
    })
  }

  /** Entrypoint for listing all method entry requests. */
  def listEntries(m: Map[String, Any]) = {
    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    def pstr(p: Boolean): String = if (p) "Pending" else "Active"

    jvms.foreach(s => {
      writeLine(s"<= JVM ${s.uniqueId} =>")
      s.methodEntryRequests
        .map(m => s"${m.className}.${m.methodName} (${pstr(m.isPending)})")
        .foreach(writeLine)
    })
  }

  /** Entrypoint for listing all method exit requests. */
  def listExits(m: Map[String, Any]) = {
    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    def pstr(p: Boolean): String = if (p) "Pending" else "Active"

    jvms.foreach(s => {
      writeLine(s"<= JVM ${s.uniqueId} =>")
      s.methodExitRequests
        .map(m => s"${m.className}.${m.methodName} (${pstr(m.isPending)})")
        .foreach(writeLine)
    })
  }

  /** Entrypoint for clearing a method entry request. */
  def clearEntry(m: Map[String, Any]) = {
    val className = m.get("class").map(_.toString)
    val methodName = m.get("method").map(_.toString)

    // Require class name if method name provided
    if (methodName.nonEmpty && className.isEmpty)
      throw new RuntimeException("Missing class argument!")

    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    jvms.foreach(jvm => {
      if (className.nonEmpty && methodName.nonEmpty)
        jvm.removeMethodEntryRequests(className.get, methodName.get)
      else if (className.nonEmpty)
        jvm.methodEntryRequests.filter(_.className == className.get)
          .foreach(r =>
            jvm.removeMethodEntryRequests(r.className, r.methodName)
          )
      else
        jvm.removeAllMethodEntryRequests()
    })
  }

  /** Entrypoint for clearing a method exit request. */
  def clearExit(m: Map[String, Any]) = {
    val className = m.get("class").map(_.toString)
    val methodName = m.get("method").map(_.toString)

    // Require class name if method name provided
    if (methodName.nonEmpty && className.isEmpty)
      throw new RuntimeException("Missing class argument!")

    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    jvms.foreach(jvm => {
      if (className.nonEmpty && methodName.nonEmpty)
        jvm.removeMethodExitRequests(className.get, methodName.get)
      else if (className.nonEmpty)
        jvm.methodExitRequests.filter(_.className == className.get)
          .foreach(r => jvm.removeMethodExitRequests(r.className, r.methodName))
      else
        jvm.removeAllMethodExitRequests()
    })
  }
}
