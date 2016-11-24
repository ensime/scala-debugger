package org.scaladebugger.tool.commands

import org.scaladebugger.api.debuggers.LaunchingDebugger
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.tool.Repl
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

import scala.util.{Failure, Try}

class LaunchCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("LaunchCommand") {
    it("should launch and attach to the specified class") {
      val testClass = "org.scaladebugger.test.misc.LaunchingMain"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val q = "\""
      val terminal = newVirtualTerminal()
      val repl = Repl.newInstance(newTerminal = (_,_) => terminal)

      // Queue up attach action
      terminal.newInputLine(s"launch $q$testClass$q")

      // Start processing input
      // TODO: Add repl stop code regardless of test success
      repl.start()

      // Eventually, launch should complete
      logTimeTaken(eventually {
        repl.stateManager.state.activeDebugger should not be (None)
        repl.stateManager.state.scalaVirtualMachines should not be (empty)
      })

      // Finished
      repl.stop()
    }
  }
}
