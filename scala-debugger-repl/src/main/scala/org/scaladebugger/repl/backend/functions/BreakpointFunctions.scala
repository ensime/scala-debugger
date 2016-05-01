package org.scaladebugger.repl.backend.functions
import acyclic.file

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.repl.backend.StateManager
import org.scaladebugger.api.dsl.Implicits._

/**
 * Represents a collection of functions for managing breakpoints.
 *
 * @param stateManager The manager whose state to share among functions
 */
class BreakpointFunctions(private val stateManager: StateManager) {
  /** Entrypoint for creating a breakpoint. */
  def createBreakpoint(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val file = m.get("file").map(_.toString).getOrElse(
      throw new RuntimeException("Missing file argument!")
    )

    val line = m.get("line").map(_.toString.toDouble.toInt).getOrElse(
      throw new RuntimeException("Missing line argument!")
    )

    jvms.foreach(s => {
      s.onUnsafeBreakpoint(file, line, NoResume).foreach(e => {
        val t = e.thread()
        val loc = e.location()
        val locString = s"${loc.sourceName()}:${loc.lineNumber()}"

        println(s"Breakpoint hit at $locString")
      })
    })
  }

  /** Entrypoint for listing all breakpoints. */
  def listBreakpoints(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    jvms.foreach(s => {
      println(s"<= JVM ${s.uniqueId} =>")
      s.breakpointRequests.map(b => s"${b.fileName}:${b.lineNumber}")
          .foreach(println)
    })
  }

  /** Entrypoint for clearing a breakpoint. */
  def clearBreakpoint(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val file = m.get("file").map(_.toString)
    val line = m.get("line").map(_.toString.toDouble.toInt)

    if (file.nonEmpty ^ line.nonEmpty) println("Missing file or line argument!")

    import org.scaladebugger.api.profiles.Constants.CloseRemoveAll
    if (file.nonEmpty && line.nonEmpty) {
      jvms.foreach(s => {
        s.breakpointRequests
          .filter(b => b.fileName == file.get && b.lineNumber == line.get)
          .map(b => s.onUnsafeBreakpoint(b.fileName, b.lineNumber))
          .foreach(_.close(data = CloseRemoveAll))
      })
    } else if (file.isEmpty && line.isEmpty) {
      jvms.foreach(s => {
        s.breakpointRequests
          .map(b => s.onUnsafeBreakpoint(b.fileName, b.lineNumber))
          .foreach(_.close(data = CloseRemoveAll))
      })
    }
  }
}
