package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class ThreadResumeCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures with MockFactory
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("ThreadResumeCommand") {
    it("should resume the specific thread if given a name") {
      val testClass = "org.scaladebugger.test.misc.MainUsingApp"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val threadName = "main"

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

          // Verify main thread is suspended
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val thread = svm.thread(threadName)
            thread.status.isSuspended should be (true)
            thread.status.isAtBreakpoint should be (true)
          }

          // Resume the thread
          vt.newInputLine(s"resume $q$threadName$q")

          // Verify main thread is resumed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            println("Scala Virtual Machine: " + svm.uniqueId)
            val thread = svm.thread(threadName)
            println("Thread: " + thread.name)
            println("Suspended: " + thread.status.isSuspended)
            println("At Breakpoint: " + thread.status.isAtBreakpoint)
            thread.status.isSuspended should be (false)
            thread.status.isAtBreakpoint should be (false)
          }
        })
      }
    }

    it("should resume all threads if no thread name provided") {
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

          // Resume all threads
          vt.newInputLine("resume")

          // Verify all threads resumed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            svm.threads.forall(_.status.isSuspended) should be (false)
          }
        })
      }
    }
  }
}
