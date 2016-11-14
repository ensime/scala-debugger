package org.scaladebugger.tool.commands

import java.net.URI
import org.scaladebugger.tool.Repl
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class SourcepathCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures with MockFactory
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("SourcepathCommand") {
    it("should add the provided path to the current list") {
      val vt = newVirtualTerminal()
      val repl = Repl.newInstance(mainTerminal = vt)
      repl.start()

      val q = "\""
      val s = java.io.File.separator

      // Set some paths to be displayed
      repl.stateManager.updateSourcePaths(Seq(
        new URI(s"${s}a"),
        new URI(s"${s}b"),
        new URI(s"${s}c")
      ))

      // Add 'd' as sourcepath
      vt.newInputLine(s"sourcepath $q${s}d$q")

      eventually {
        val state = repl.stateManager.state
        state.sourcePaths.map(_.getPath) should contain (s"${s}d")
      }
    }

    it("should list all current source paths if no argument provided") {
      val vt = newVirtualTerminal()
      val repl = Repl.newInstance(mainTerminal = vt)
      repl.start()

      // Set some paths to be displayed
      repl.stateManager.updateSourcePaths(Seq(
        new URI("a"),
        new URI("b"),
        new URI("c")
      ))

      // Display the source paths
      vt.newInputLine("sourcepath")

      val line = vt.nextOutputLine(
        waitTime = Constants.NextOutputLineTimeout.millisPart
      )
      val s = java.io.File.pathSeparator
      line.get should be (s"Source paths: a${s}b${s}c\n")
    }
  }
}
