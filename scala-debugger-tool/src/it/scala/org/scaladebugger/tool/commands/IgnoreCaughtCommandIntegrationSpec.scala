package org.scaladebugger.tool.commands

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class IgnoreCaughtCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("IgnoreCaughtCommand") {
    it("should delete a specific pending caught exception request by class name") {
      val testClass = "org.scaladebugger.test.exceptions.InsideTryBlockException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testExceptionName = "org.scaladebugger.test.exceptions.CustomException"
      val testFakeExceptionName = "org.invalid.exception"

      // Create exception requests before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"catch $q$testExceptionName$q")
      virtualTerminal.newInputLine(s"catchc $q$testExceptionName$q")
      virtualTerminal.newInputLine(s"catchu $q$testExceptionName$q")
      virtualTerminal.newInputLine(s"catch $q$testFakeExceptionName$q")
      virtualTerminal.newInputLine(s"catchc $q$testFakeExceptionName$q")
      virtualTerminal.newInputLine(s"catchu $q$testFakeExceptionName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our exception requests were made
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false),
              (testExceptionName, true, false, false),
              (testExceptionName, false, true, false),
              (testFakeExceptionName, true, true, true),
              (testFakeExceptionName, true, false, true),
              (testFakeExceptionName, false, true, true)
            )
          }

          // Delete the exception requests that are pending
          vt.newInputLine(s"ignorec $q$testFakeExceptionName$q")

          // Verify our exception requests were deleted
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false),
              (testExceptionName, true, false, false),
              (testExceptionName, false, true, false),
              (testFakeExceptionName, true, true, true),
              (testFakeExceptionName, false, true, true)
            )
          }
        })
      }
    }

    it("should delete a specific active caught exception request by class name") {
      val testClass = "org.scaladebugger.test.exceptions.InsideTryBlockException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testExceptionName = "org.scaladebugger.test.exceptions.CustomException"
      val testFakeExceptionName = "org.invalid.exception"

      // Create exception requests before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"catch $q$testExceptionName$q")
      virtualTerminal.newInputLine(s"catchc $q$testExceptionName$q")
      virtualTerminal.newInputLine(s"catchu $q$testExceptionName$q")
      virtualTerminal.newInputLine(s"catch $q$testFakeExceptionName$q")
      virtualTerminal.newInputLine(s"catchc $q$testFakeExceptionName$q")
      virtualTerminal.newInputLine(s"catchu $q$testFakeExceptionName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our exception requests were made
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false),
              (testExceptionName, true, false, false),
              (testExceptionName, false, true, false),
              (testFakeExceptionName, true, true, true),
              (testFakeExceptionName, true, false, true),
              (testFakeExceptionName, false, true, true)
            )
          }

          // Delete the exception requests that are active
          vt.newInputLine(s"ignorec $q$testExceptionName$q")

          // Verify our exception requests were deleted
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false),
              (testExceptionName, false, true, false),
              (testFakeExceptionName, true, true, true),
              (testFakeExceptionName, true, false, true),
              (testFakeExceptionName, false, true, true)
            )
          }
        })
      }
    }

    it("should delete all pending and active caught exception requests matching a wildcard") {
      val testClass = "org.scaladebugger.test.exceptions.InsideTryBlockException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testExceptionName = "org.scaladebugger.test.exceptions.CustomException"
      val testFakeExceptionName = "org.invalid.exception"

      // Create exception requests before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"catch $q$testExceptionName$q")
      virtualTerminal.newInputLine(s"catchc $q$testExceptionName$q")
      virtualTerminal.newInputLine(s"catchu $q$testExceptionName$q")
      virtualTerminal.newInputLine(s"catch $q$testFakeExceptionName$q")
      virtualTerminal.newInputLine(s"catchc $q$testFakeExceptionName$q")
      virtualTerminal.newInputLine(s"catchu $q$testFakeExceptionName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our exception requests were made
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false),
              (testExceptionName, true, false, false),
              (testExceptionName, false, true, false),
              (testFakeExceptionName, true, true, true),
              (testFakeExceptionName, true, false, true),
              (testFakeExceptionName, false, true, true)
            )
          }

          // Delete the exception requests
          vt.newInputLine("ignorec \"org.*\"")

          // Verify our exception requests were deleted
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false),
              (testExceptionName, false, true, false),
              (testFakeExceptionName, true, true, true),
              (testFakeExceptionName, false, true, true)
            )
          }
        })
      }
    }

    it("should delete all pending and active caught exception requests if no class name given") {
      val testClass = "org.scaladebugger.test.exceptions.InsideTryBlockException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testExceptionName = "org.scaladebugger.test.exceptions.CustomException"
      val testFakeExceptionName = "org.invalid.exception"

      // Create exception requests before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"catch $q$testExceptionName$q")
      virtualTerminal.newInputLine(s"catchc $q$testExceptionName$q")
      virtualTerminal.newInputLine(s"catchu $q$testExceptionName$q")
      virtualTerminal.newInputLine(s"catch $q$testFakeExceptionName$q")
      virtualTerminal.newInputLine(s"catchc $q$testFakeExceptionName$q")
      virtualTerminal.newInputLine(s"catchu $q$testFakeExceptionName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our exception requests were made
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false),
              (testExceptionName, true, false, false),
              (testExceptionName, false, true, false),
              (testFakeExceptionName, true, true, true),
              (testFakeExceptionName, true, false, true),
              (testFakeExceptionName, false, true, true)
            )
          }

          // Delete the exception requests
          vt.newInputLine(s"ignorec")

          // Verify our exception requests were deleted
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false),
              (testExceptionName, false, true, false),
              (testFakeExceptionName, true, true, true),
              (testFakeExceptionName, false, true, true)
            )
          }
        })
      }
    }
  }
}
