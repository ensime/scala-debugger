package org.scaladebugger.tool.commands

import java.nio.file.Paths

import org.scaladebugger.test.helpers.FixedParallelSuite
import org.scaladebugger.tool.Repl
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{ToolConstants, ToolTestUtilities, ToolFixtures}

class SourcepathCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures with MockFactory
  with ToolTestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("SourcepathCommand") {
    it("should add the provided path to the current list and load sources") {
      val vt = newVirtualTerminal()
      val repl = Repl.newInstance(newTerminal = (_,_) => vt)
      repl.start()

      val q = "\""
      val s = java.io.File.separator

      // Set some paths to be displayed
      repl.stateManager.updateSourcePaths(Seq(
        Paths.get("a"),
        Paths.get("b"),
        Paths.get("c")
      ))

      // Add '.' as sourcepath
      vt.newInputLine("sourcepath \".\"")

      // Verify that we have finished loading our source files
      eventually {
        validateNextLine(vt, """Loaded \d+ source files""",
          success = (text, line) => line should startWith regex text)
      }

      eventually {
        val state = repl.stateManager.state
        state.sourcePaths.last.getFileName.toString should contain ('.')
      }
    }

    it("should list all current source paths if no argument provided") {
      val vt = newVirtualTerminal()
      val repl = Repl.newInstance(newTerminal = (_,_) => vt)
      repl.start()

      // Set some paths to be displayed
      repl.stateManager.updateSourcePaths(Seq(
        Paths.get("a"),
        Paths.get("b"),
        Paths.get("c")
      ))

      // Display the source paths
      vt.newInputLine("sourcepath")

      val line = vt.nextOutputLine(
        waitTime = ToolConstants.NextOutputLineTimeout.millisPart
      )
      val s = java.io.File.pathSeparator
      line.get should be (s"Source paths: a${s}b${s}c\n")
    }
  }
}
