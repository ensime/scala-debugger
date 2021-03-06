package org.scaladebugger.tool.commands

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class IgnoreUncaughtCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("IgnoreUncaughtCommand") {
    it("should delete a specific pending uncaught exception request by class name") {
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
          vt.newInputLine(s"ignoreu $q$testFakeExceptionName$q")

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
              (testFakeExceptionName, true, false, true)
            )
          }
        })
      }
    }

    it("should delete a specific active uncaught exception request by class name") {
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
          vt.newInputLine(s"ignoreu $q$testExceptionName$q")

          // Verify our exception requests were deleted
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false),
              (testExceptionName, true, false, false),
              (testFakeExceptionName, true, true, true),
              (testFakeExceptionName, true, false, true),
              (testFakeExceptionName, false, true, true)
            )
          }
        })
      }
    }

    it("should delete all pending and active uncaught exception requests matching a wildcard") {
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
          vt.newInputLine("ignoreu \"org.*\"")

          // Verify our exception requests were deleted
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false),
              (testExceptionName, true, false, false),
              (testFakeExceptionName, true, true, true),
              (testFakeExceptionName, true, false, true)
            )
          }
        })
      }
    }

    it("should delete all pending and active uncaught exception requests if no class name given") {
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
          vt.newInputLine(s"ignoreu")

          // Verify our exception requests were deleted
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests.map(er =>
              (er.className, er.notifyCaught, er.notifyUncaught, er.isPending))
            ers should contain theSameElementsAs Seq(
              (testExceptionName, true, true, false),
              (testExceptionName, true, false, false),
              (testFakeExceptionName, true, true, true),
              (testFakeExceptionName, true, false, true)
            )
          }
        })
      }
    }
  }
}
