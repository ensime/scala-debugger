package org.scaladebugger.tool.commands

import java.net.URI
import java.nio.file.Paths

import org.scaladebugger.tool.Repl
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class SourcepathClearCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures with MockFactory
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
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
