package org.scaladebugger.tool.commands

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scaladebugger.tool.Repl
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class LaunchCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
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
