package org.scaladebugger.tool.backend.functions
import acyclic.file

import java.io.File
import java.nio.file.Paths

import org.scaladebugger.tool.backend.StateManager

/**
 * Represents a collection of functions for inspecting source code.
 *
 * @param stateManager The manager whose state to share among functions
 * @param writeLine Used to write output to the terminal
 */
class SourceFunctions(
  private val stateManager: StateManager,
  private val writeLine: String => Unit
) {
  /** Entrypoint for printing source code. */
  def list(m: Map[String, Any]) = {
    val thread = stateManager.state.activeThread
    if (thread.isEmpty) writeLine("No active thread!")

    val size = m.getOrElse("size", 4).toString.toInt

    // Get sources and add '.' as a source
    val sources = stateManager.state.sourcePaths :+ new File(".").toURI

    thread.foreach(t => {
      t.suspend()

      // Find current location
      val location = t.topFrame.location
      val filePath = location.sourcePath
      val lineNumber = location.lineNumber

      t.resume()

      // Look up potential source location
      val file = sources
        .map(s => Paths.get(s).resolve(filePath))
        .map(_.toFile)
        .find(f => f.exists() && f.isFile)

      if (file.isEmpty) writeLine(s"Source not found for $filePath!")

      // Display source code
      file.foreach(f => {
        val lines = scala.io.Source.fromFile(f).getLines()
        val startLine = Math.max(lineNumber - size, 1)
        val endLine = lineNumber + size

        // Skip up to five lines prior
        (1 until startLine).foreach(_ => if (lines.hasNext) lines.next())

        // Display lines up to and five past the line number
        (startLine to endLine).foreach(i => {
          if (lines.hasNext) {
            val line = lines.next()
            val lineMarker = s"$i ${if (i == lineNumber) "=>" else "  "}"
            writeLine(s"$lineMarker\t$line")
          }
        })
      })
    })
  }

  /** Entrypoint for displaying or changing the source path. */
  def sourcepath(m: Map[String, Any]) = {
    m.get("sourcepath").map(_.toString) match {
      case Some(source) =>
        stateManager.addSourcePath(new File(source).toURI)
      case None =>
        val sourcePaths = stateManager.state.sourcePaths.map(_.getPath)
        writeLine(s"Source paths: ${sourcePaths.mkString(java.io.File.pathSeparator)}")
    }
  }

  /** Entrypoint for clearing the source path. */
  def sourcepathClear(m: Map[String, Any]) = {
    stateManager.updateSourcePaths(Nil)
  }

  /** Entrypoint for printing classpath info from connected JVMs. */
  def classpath(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) writeLine("No VM connected!")

    jvms.foreach(s => {
      writeLine(s"<= JVM ${s.uniqueId} =>")

      // TODO: Implement using expression evaluator
      writeLine("TODO: Implement using expression evaluator")
    })
  }
}
