package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class ThreadSuspendCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("ThreadSuspendCommand") {
    it("should suspend the specific thread if given a name") {
      val testClass = "org.scaladebugger.test.misc.MainUsingApp"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create a breakpoint before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:11\n")
          }

          // Find a thread that isn't the main thread currently suspended
          val svm = sm.state.scalaVirtualMachines.head
          val thread = svm.threads.find(_.name != "main").get
          val threadName = thread.name

          // Suspend the thread
          vt.newInputLine(s"suspend $q$threadName$q")

          // Verify the thread was suspended by us
          eventually {
            thread.status.isSuspended should be (true)
            thread.status.isAtBreakpoint should be (false)
          }
        })
      }
    }

    it("should suspend all threads if no thread name provided") {
      val testClass = "org.scaladebugger.test.misc.MainUsingApp"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create a breakpoint before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:11\n")
          }

          // Suspend the threads
          vt.newInputLine("suspend")

          // Verify all threads suspended
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            svm.threads.forall(_.status.isSuspended) should be (true)
          }
        })
      }
    }
  }
}
