package org.scaladebugger.tool.backend.functions

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.wrappers.Implicits._
import org.scaladebugger.tool.backend.StateManager

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a collection of functions for managing threads.
 *
 * @param stateManager The manager whose state to share among functions
 * @param writeLine Used to write output to the terminal
 */
class ThreadGroupFunctions(
  private val stateManager: StateManager,
  private val writeLine: String => Unit
) {
  /** Entrypoint for listing thread groups for connected JVMs. */
  def threadsGroups(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) writeLine("No VM connected!")

    jvms.foreach(s => {
      val threadGroups = s.threads.map(_.threadGroup)
        .groupBy(_.name).map(_._2.head)

      writeLine(s"<= JVM ${s.uniqueId} =>")
      threadGroups.foreach(tg => {
        val rName = tg.referenceType.name
        val id = "0x" + tg.uniqueIdHexString
        val name = tg.name

        writeLine(s"($rName)$id $name")
      })
    })
  }

  /** Entrypoint for setting the default thread group. */
  def threadGroup(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) writeLine("No VM connected!")

    val threadGroupName = m.get("threadGroup").map(_.toString)

    threadGroupName match {
      // If name provided, lookup and set as active thread group
      case Some(name) =>
        val threadGroup = jvms.view.flatMap(_.threads).map(_.threadGroup)
          .collectFirst { case tg if tg.name == name => tg }

        if (threadGroup.isEmpty) writeLine(s"No thread group found named '$name'!")
        threadGroup.foreach(stateManager.updateActiveThreadGroup)

      // No name provided, so clear existing active thread group
      case None =>
        stateManager.clearActiveThreadGroup()
    }
  }
}
