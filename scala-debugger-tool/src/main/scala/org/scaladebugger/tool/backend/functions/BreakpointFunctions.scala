package org.scaladebugger.tool.backend.functions

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.tool.backend.StateManager

/**
 * Represents a collection of functions for managing breakpoints.
 *
 * @param stateManager The manager whose state to share among functions
 * @param writeLine Used to write output to the terminal
 */
class BreakpointFunctions(
  private val stateManager: StateManager,
  private val writeLine: String => Unit
) {
  /** Entrypoint for creating a breakpoint. */
  def createBreakpoint(m: Map[String, Any]) = {
    val file = m.get("file").map(_.toString).getOrElse(
      throw new RuntimeException("Missing file argument!")
    )

    val line = m.get("line").map(_.toString.toDouble.toInt).getOrElse(
      throw new RuntimeException("Missing line argument!")
    )

    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      val dsvm = stateManager.state.dummyScalaVirtualMachine
      jvms = Seq(dsvm)
    }

    writeLine(s"Set breakpoint at $file:$line")
    jvms.foreach(s => {
      s.getOrCreateBreakpointRequest(file, line, NoResume).foreach(e => {
        val t = e.thread
        val loc = e.location
        val locString = s"${loc.sourceName}:${loc.lineNumber}"

        writeLine(s"Breakpoint hit at $locString")
      })
    })
  }

  /** Entrypoint for listing all breakpoints. */
  def listBreakpoints(m: Map[String, Any]) = {
    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    def pstr(p: Boolean): String = if (p) "Pending" else "Active"

    jvms.foreach(s => {
      writeLine(s"<= JVM ${s.uniqueId} =>")
      s.breakpointRequests
        .map(b => s"${b.fileName}:${b.lineNumber} (${pstr(b.isPending)})")
        .foreach(writeLine)
    })
  }

  /** Entrypoint for clearing a breakpoint. */
  def clearBreakpoint(m: Map[String, Any]) = {
    val file = m.get("file").map(_.toString)
    val line = m.get("line").map(_.toString.toDouble.toInt)

    if (file.nonEmpty ^ line.nonEmpty)
      writeLine("Missing file or line argument!")

    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    if (file.nonEmpty && line.nonEmpty) {
      writeLine(s"Cleared breakpoint at ${file.get}:${line.get}")
      jvms.foreach(_.removeBreakpointRequests(file.get, line.get))
    } else if (file.isEmpty && line.isEmpty) {
      writeLine("Cleared all breakpoints")
      jvms.foreach(_.removeAllBreakpointRequests())
    }
  }
}
