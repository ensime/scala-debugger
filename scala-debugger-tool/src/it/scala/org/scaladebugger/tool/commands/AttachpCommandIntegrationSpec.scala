package org.scaladebugger.tool.commands

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.tool.Repl
import org.scaladebugger.tool.frontend.VirtualTerminal
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, ToolFixtures, Constants}

class AttachpCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("AttachpCommand") {
    it("should attach successfully using a pid") {
      val testClass = "org.scaladebugger.test.misc.AttachingMain"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withProcessPid(testClass) { (pid) =>
        val terminal = new VirtualTerminal()

        val repl = Repl.newInstance(mainTerminal = terminal)

        // Queue up attach action
        terminal.newInputLine(s"attachp $pid")

        // Start processing input
        // TODO: Add repl stop code regardless of test success
        repl.start()

        // Eventually, attach should complete
        logTimeTaken({
          eventually {
            val output = terminal.nextOutputLine(waitTime = 100)
            output.get should startWith ("Attached with id")
          }

          eventually {
            repl.stateManager.state.activeDebugger should not be None
          }
        })
      }
    }
  }
}
