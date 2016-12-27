package org.scaladebugger.tool.commands

import java.nio.file.Paths

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scaladebugger.tool.Repl
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class SourcepathClearCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("SourcepathCommand") {
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

      // Clear the source paths
      vt.newInputLine("sourcepathclear")

      eventually {
        val state = repl.stateManager.state
        state.sourcePaths should be (empty)
      }
    }
  }
}
