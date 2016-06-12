package org.scaladebugger.repl.backend.functions
import acyclic.file

import org.scaladebugger.repl.backend.StateManager
import org.scaladebugger.repl.backend.utils.Regex

/**
 * Represents a collection of functions for managing methods.
 *
 * @param stateManager The manager whose state to share among functions
 */
class ClassFunctions(private val stateManager: StateManager) {
  /** Entrypoint for listing classes. */
  def classes(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val filter = m.get("filter").map(_.toString).getOrElse("*")

    jvms.foreach(s => {
      println(s"<= ${s.uniqueId} =>")

      // Wildcard matching
      val r = Regex.wildcardString(filter)

      s.lowlevel.classManager.allClasses.map(_.name()).filter(_.matches(r))
        .foreach(println)
    })
  }

  /** Entrypoint for listing methods for a class. */
  def methods(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    jvms.foreach(s => {
      println(s"<= ${s.uniqueId} =>")

      import scala.collection.JavaConverters._
      s.lowlevel.classManager.allClasses
        .find(_.name() == className)
        .map(_.allMethods().asScala)
        .foreach(_.foreach(println))
    })
  }

  /** Entrypoint for listing fields for a class. */
  def fields(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    jvms.foreach(s => {
      println(s"<= ${s.uniqueId} =>")

      import scala.collection.JavaConverters._
      s.lowlevel.classManager.allClasses
        .find(_.name() == className)
        .map(_.allFields().asScala)
        .foreach(_.foreach(println))
    })
  }
}
