package org.scaladebugger.tool.commands

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class CatchUncaughtCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("CatchUncaughtCommand") {
    it("should catch all uncaught exceptions if no filter provided") {
      val testClass = "org.scaladebugger.test.exceptions.OutsideTryException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testExceptionName = "org.scaladebugger.test.exceptions.CustomException"

      // Create exception request before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine("catchu")

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
              (true, false, true, false)
            )
          }

          // Verify that we hit our custom exception
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

    it("should catch all uncaught exceptions matching the provided filter") {
      val testClass = "org.scaladebugger.test.exceptions.OutsideTryException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testExceptionName = "org.scaladebugger.test.exceptions.CustomException"
      val testExceptionFilter = "*.CustomException"

      // Create exception request before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"catchu $q$testExceptionFilter$q")

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
              (true, false, true, false)
            )
          }

          // Verify that we hit the custom exception
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

    it("should catch the specified exception (uncaught)") {
      val testClass = "org.scaladebugger.test.exceptions.OutsideTryException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testExceptionName = "org.scaladebugger.test.exceptions.CustomException"

      // Create exception request before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"catchu $q$testExceptionName$q")

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
              (testExceptionName, false, true, false)
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
