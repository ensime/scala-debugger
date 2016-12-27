package org.scaladebugger.tool.commands

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scaladebugger.tool.Repl
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class AttachpCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("AttachpCommand") {
    it("should attach successfully using a pid") {
      val testClass = "org.scaladebugger.test.misc.AttachingMain"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withProcessPid(testClass) { (pid) =>
        val terminal = newVirtualTerminal()

        val repl = Repl.newInstance(newTerminal = (_,_) => terminal)

        // Queue up attach action
        terminal.newInputLine(s"attachp $pid")

        // Start processing input
        // TODO: Add repl stop code regardless of test success
        repl.start()

        // Eventually, attach should complete
        logTimeTaken(eventually {
          repl.stateManager.state.activeDebugger should not be None
          repl.stateManager.state.scalaVirtualMachines should not be (empty)
        })

        // Finished
        repl.stop()
      }
    }
  }
}
