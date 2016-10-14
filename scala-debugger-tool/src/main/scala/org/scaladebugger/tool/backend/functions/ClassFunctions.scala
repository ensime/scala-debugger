package org.scaladebugger.tool.backend.functions
import acyclic.file

import org.scaladebugger.tool.backend.StateManager
import org.scaladebugger.tool.backend.utils.Regex

/**
 * Represents a collection of functions for managing methods.
 *
 * @param stateManager The manager whose state to share among functions
 * @param writeLine Used to write output to the terminal
 */
class ClassFunctions(
  private val stateManager: StateManager,
  private val writeLine: String => Unit
) {
  /** Entrypoint for listing classes. */
  def classes(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) writeLine("No VM connected!")

    val filter = m.get("filter").map(_.toString).getOrElse("*")

    jvms.foreach(s => {
      writeLine(s"<= ${s.uniqueId} =>")

      // Wildcard matching
      val r = Regex.wildcardString(filter)

      s.classes.map(_.name).filter(_.matches(r)).foreach(writeLine)
    })
  }

  /** Entrypoint for listing methods for a class. */
  def methods(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) writeLine("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    jvms.foreach(s => {
      writeLine(s"<= ${s.uniqueId} =>")

      s.classOption(className)
        .map(_.allMethods)
        .map(_.map(_.name))
        .map(_.mkString("\n"))
        .foreach(writeLine)
    })
  }

  /** Entrypoint for listing fields for a class. */
  def fields(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) writeLine("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    jvms.foreach(s => {
      writeLine(s"<= ${s.uniqueId} =>")

      s.classOption(className)
        .map(_.allFields)
        .map(_.map(_.name))
        .map(_.mkString("\n"))
        .foreach(writeLine)
    })
  }
}
