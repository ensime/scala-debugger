package org.scaladebugger.repl.backend.functions
import acyclic.file

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.wrappers.Implicits._
import org.scaladebugger.repl.backend.StateManager

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a collection of functions for managing threads.
 *
 * @param stateManager The manager whose state to share among functions
 */
class ThreadGroupFunctions(private val stateManager: StateManager) {
  /** Entrypoint for listing thread groups for connected JVMs. */
  def threadsGroups(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    jvms.foreach(s => {
      val threadGroups = s.underlyingVirtualMachine
        .allThreads().asScala.map(_.threadGroup()).distinct

      println(s"<= JVM ${s.uniqueId} =>")
      threadGroups.foreach(tg => {
        val rName = tg.referenceType().name()
        val id = "0x" + tg.uniqueID().toHexString
        val name = tg.name()

        println(s"($rName)$id $name")
      })
    })
  }

  /** Entrypoint for setting the default thread group. */
  def threadGroup(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val threadGroupName = m.get("threadGroup").map(_.toString)

    threadGroupName match {
      // If name provided, lookup and set as active thread group
      case Some(name) =>
        val threadGroup = jvms.view.flatMap(
          _.underlyingVirtualMachine.allThreads().asScala
            .map(_.threadGroup()).distinct
        ).collectFirst {
          case tg if tg.name() == name => tg
        }

        if (threadGroup.isEmpty) println(s"No thread group found named '$name'!")
        threadGroup.foreach(stateManager.updateActiveThreadGroup)

      // No name provided, so clear existing active thread group
      case None =>
        stateManager.clearActiveThreadGroup()
    }
  }
}
