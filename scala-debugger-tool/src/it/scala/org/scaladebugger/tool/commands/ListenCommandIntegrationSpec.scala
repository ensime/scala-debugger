package org.scaladebugger.tool.commands

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scaladebugger.tool.Repl
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class ListenCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("ListenCommand") {
    it("should receive a connection from a remote JVM on the desired port") {
      val testClass = "org.scaladebugger.test.misc.ListeningMain"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      JDITools.usingOpenPort(port => {
        val terminal = newVirtualTerminal()

        val repl = Repl.newInstance(newTerminal = (_,_) => terminal)

        // Listen on provided port
        terminal.newInputLine(s"listen $port")

        // Start processing input
        // TODO: Add repl stop code regardless of test success
        repl.start()

        // Wait for debugger to be running before spawning process
        eventually {
          val d = repl.stateManager.state.activeDebugger
          d should not be None
          d.get.isRunning should be (true)
        }

        // Create a process to attach to our listening debugger
        // TODO: Destroy process regardless of test success
        val p = JDITools.spawn(
          className = testClass,
          options = Seq("-classpath", JDITools.jvmClassPath),
          port = port,
          server = false
        )

        // Eventually, listen should complete
        logTimeTaken(eventually {
          repl.stateManager.state.activeDebugger should not be None
          repl.stateManager.state.scalaVirtualMachines should not be (empty)
        })

        // Finished
        p.destroy()
        repl.stop()
      })
    }
  }
}
