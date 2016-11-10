package org.scaladebugger.tool.commands

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class CatchBothCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("CatchBothCommand") {
    it("should catch all exceptions if no filter provided") {
      val testClass = "org.scaladebugger.test.exceptions.InsideTryBlockException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      // Create exception request before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine("catch")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our exception request was made
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.isCatchall, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (true, true, true, false)
            )
          }

          // Verify that we hit some exception (classloader exception)
          eventually {
            validateNextLine(
              vt, "Caught .*? detected",
              success = (text, line) => line should startWith regex text
            )
          }

          // Main thread should be suspended
          eventually {
            val svm = sm.state.scalaVirtualMachines.head

            // NOTE: Using assert for better error message
            assert(svm.thread("main").status.isSuspended,
              "Main thread was not suspended!")
          }
        })
      }
    }

    it("should catch all exceptions matching the provided filter") {
      val testClass = "org.scaladebugger.test.exceptions.InsideTryBlockException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testExceptionName = "org.scaladebugger.test.exceptions.CustomException"
      val testExceptionFilter = "*.CustomException"

      // Create exception request before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"catch $q$testExceptionFilter$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our exception request was made
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.isCatchall, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (true, true, true, false)
            )
          }

          // Verify that we hit the custom exception
          eventually {
            validateNextLine(
              vt, s"Caught $testExceptionName detected",
              success = (text, line) => line should startWith (text)
            )
          }

          // Main thread should be suspended
          eventually {
            val svm = sm.state.scalaVirtualMachines.head

            // NOTE: Using assert for better error message
            assert(svm.thread("main").status.isSuspended,
              "Main thread was not suspended!")
          }
        })
      }
    }

    it("should catch the specified exception (caught)") {
      val testClass = "org.scaladebugger.test.exceptions.InsideTryBlockException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testExceptionName = "org.scaladebugger.test.exceptions.CustomException"

      // Create exception request before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"catch $q$testExceptionName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our exception request was made
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false)
            )
          }

          // Verify that we hit the exception
          eventually {
            validateNextLine(
              vt, s"Caught $testExceptionName detected",
              success = (text, line) => line should startWith (text)
            )
          }

          // Main thread should be suspended
          eventually {
            val svm = sm.state.scalaVirtualMachines.head

            // NOTE: Using assert for better error message
            assert(svm.thread("main").status.isSuspended,
              "Main thread was not suspended!")
          }
        })
      }
    }

    it("should catch the specified exception (uncaught)") {
      val testClass = "org.scaladebugger.test.exceptions.OutsideTryException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testExceptionName = "org.scaladebugger.test.exceptions.CustomException"

      // Create exception request before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"catch $q$testExceptionName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our exception request was made
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false)
            )
          }

          // Verify that we hit the exception
          eventually {
            validateNextLine(
              vt, s"Uncaught $testExceptionName detected",
              success = (text, line) => line should startWith (text)
            )
          }

          // Main thread should be suspended
          eventually {
            val svm = sm.state.scalaVirtualMachines.head

            // NOTE: Using assert for better error message
            assert(svm.thread("main").status.isSuspended,
              "Main thread was not suspended!")
          }
        })
      }
    }
  }
}
