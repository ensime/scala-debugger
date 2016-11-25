package org.scaladebugger.tool.backend.functions
import acyclic.file
import java.nio.file._

import org.scaladebugger.api.utils.FileSearcher
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
  import scala.collection.mutable.{Map => mutMap}
  private lazy val cache: mutMap[String, Seq[Path]] = mutMap()

  /** Generates a source map from the provided source paths. */
  private def generateSourceMap(sourcePaths: Path*): Map[String, Seq[Path]] = {
    // Search for Scala/Java source files
    val files = sourcePaths.flatMap(
      FileSearcher.loadFilePaths(_: Path, globString = "**.{java,scala}")
    )
    val sourceMap = files.groupBy(_.getFileName.toString)
    sourceMap
  }

  /** Loads source files for the provided source and adds to the cache. */
  def loadAndAddSource(source: String): Unit = {
    val sourcePath = Paths.get(source)

    // If the path already has been added, make a note and exit
    val existingSourcePaths = stateManager.state.sourcePaths
    if (existingSourcePaths.map(_.toString).contains(sourcePath.toString)) {
      writeLine("Source path already added!")
      writeLine(Seq(
        "If you need to reload the sources on the path,",
        "clear and re-add the desired source path!"
      ).mkString(" "))
      return
    }

    writeLine(s"Loading sources from ${sourcePath.toString}")
    val sourceMap = generateSourceMap(sourcePath)

    val sourceFileIterator = sourceMap.values.flatten
    val totalSourceFiles = sourceFileIterator.size
    writeLine(s"Loaded $totalSourceFiles source files")

    // Update our cache with the new data
    cache.synchronized(sourceMap.foreach { case (fileName, files) =>
      val cacheFiles = cache.getOrElse(fileName, Nil)
      val allFiles = files ++ cacheFiles
      cache(fileName) = allFiles.distinct
    })

    stateManager.addSourcePath(sourcePath)
  }

  def clearSources(): Unit = cache.synchronized {
    cache.clear()
    stateManager.updateSourcePaths(Nil)
  }

  /** Entrypoint for printing source code. */
  def list(m: Map[String, Any]) = cache.synchronized {
    val thread = stateManager.state.activeThread
    if (thread.isEmpty) writeLine("No active thread!")

    val size = m.getOrElse("size", 4).toString.toDouble.toInt

    thread.foreach(t => {
      val (filePath, lineNumber) = t.suspendAndExecute {
        // Find current location
        val location = t.topFrame.location
        val filePath = location.sourcePath
        val lineNumber = location.lineNumber

        (filePath, lineNumber)
      }.get

      // Get file name for path
      val fileName = Paths.get(filePath).getFileName.toString

      // Check if cached
      val sourceFiles = cache.get(fileName)

      // Find appropriate source from candidates
      val sourceFile = sourceFiles.flatMap(
        _.find(_.toString.endsWith(filePath))
      ).map(_.toFile)

      // If no file available, report issue
      if (sourceFile.isEmpty)
        writeLine(s"Source not found for $filePath!")

      // Display source code
      sourceFile.foreach(f => {
        val lines = scala.io.Source.fromFile(f).getLines()
        val startLine = Math.max(lineNumber - size, 1)
        val endLine = lineNumber + size

        // Skip up to five lines prior
        (1 until startLine).foreach(_ => if (lines.hasNext) lines.next())

        // Display lines up to and five past the line number
        val prefix = "=>"
        val ePrefix = " " * prefix.length
        (startLine to endLine).foreach(i => {
          if (lines.hasNext) {
            val line = lines.next()
            val lineMarker = s"$i ${if (i == lineNumber) prefix else ePrefix}"
            writeLine(s"$lineMarker\t$line")
          }
        })
      })
    })
  }

  /** Entrypoint for displaying or changing the source path. */
  def sourcepath(m: Map[String, Any]) = {
    m.get("sourcepath").map(_.toString) match {
      case Some(source) => loadAndAddSource(source)
      case None =>
        val sourcePaths = stateManager.state.sourcePaths
        writeLine(s"Source paths: ${sourcePaths.mkString(java.io.File.pathSeparator)}")
    }
  }

  /** Entrypoint for clearing the source path. */
  def sourcepathClear(m: Map[String, Any]) = clearSources()

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
