package org.scaladebugger.tool.commands

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class MethodExitClearCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("MethodExitClearCommand") {
    it("should delete a specific pending method exit request") {
      val testClass = "org.scaladebugger.test.methods.MethodExit"
      val testClassName = "org.scaladebugger.test.methods.MethodExitTestClass"
      val testMethodName = "testMethod"

      val testFakeClassName = "invalid.class"
      val testFakeMethodName = "fakeMethod"

      // Create method exit request before JVM starts
      val q = "\""
      val virtualTerminal = newVirtualTerminal()

      // Valid, so will be active
      virtualTerminal.newInputLine(s"mexit $q$testClassName$q $q$testMethodName$q")

      // Invalid, so will be inactive
      virtualTerminal.newInputLine(s"mexit $q$testFakeClassName$q $q$testFakeMethodName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our method exit requests were set
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodExitRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testClassName, testMethodName, false),
              (testFakeClassName, testFakeMethodName, true)
            )
          }

          // Remove the pending method exit request
          vt.newInputLine(s"mexitclear $q$testFakeClassName$q $q$testFakeMethodName$q")

          // Verify our pending method exit request was removed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodExitRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testClassName, testMethodName, false)
            )
          }
        })
      }
    }

    it("should delete a specific active method exit request") {
      val testClass = "org.scaladebugger.test.methods.MethodExit"
      val testClassName = "org.scaladebugger.test.methods.MethodExitTestClass"
      val testMethodName = "testMethod"

      val testFakeClassName = "invalid.class"
      val testFakeMethodName = "fakeMethod"

      // Create method exit requests before JVM starts
      val q = "\""
      val virtualTerminal = newVirtualTerminal()

      // Valid, so will be active
      virtualTerminal.newInputLine(s"mexit $q$testClassName$q $q$testMethodName$q")

      // Invalid, so will be inactive
      virtualTerminal.newInputLine(s"mexit $q$testFakeClassName$q $q$testFakeMethodName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our method exit requests were set
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodExitRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testClassName, testMethodName, false),
              (testFakeClassName, testFakeMethodName, true)
            )
          }

          // Remove the active method exit request
          vt.newInputLine(s"mexitclear $q$testClassName$q $q$testMethodName$q")

          // Verify our active method exit request was removed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodExitRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testFakeClassName, testFakeMethodName, true)
            )
          }
        })
      }
    }

    it("should delete all method exit requests for a specific class") {
      val testClass = "org.scaladebugger.test.methods.MethodExit"
      val testClassName = "org.scaladebugger.test.methods.MethodExitTestClass"
      val testMethodName = "testMethod"

      val testFakeClassName = "invalid.class"
      val testFakeMethodName = "fakeMethod"

      // Create method exit requests before JVM starts
      val q = "\""
      val virtualTerminal = newVirtualTerminal()

      // Valid, so will be active
      virtualTerminal.newInputLine(s"mexit $q$testClassName$q $q$testMethodName$q")

      // Invalid, so will be inactive
      virtualTerminal.newInputLine(s"mexit $q$testFakeClassName$q $q$testFakeMethodName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our method exit requests were set
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodExitRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testClassName, testMethodName, false),
              (testFakeClassName, testFakeMethodName, true)
            )
          }

          // Remove all method exit requests for a class
          vt.newInputLine(s"mexitclear $q$testClassName$q")

          // Verify our method exit requests were removed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodExitRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testFakeClassName, testFakeMethodName, true)
            )
          }
        })
      }
    }

    it("should delete all method exit requests") {
      val testClass = "org.scaladebugger.test.methods.MethodExit"
      val testClassName = "org.scaladebugger.test.methods.MethodExitTestClass"
      val testMethodName = "testMethod"

      val testFakeClassName = "invalid.class"
      val testFakeMethodName = "fakeMethod"

      // Create method exit requests before JVM starts
      val q = "\""
      val virtualTerminal = newVirtualTerminal()

      // Valid, so will be active
      virtualTerminal.newInputLine(s"mexit $q$testClassName$q $q$testMethodName$q")

      // Invalid, so will be inactive
      virtualTerminal.newInputLine(s"mexit $q$testFakeClassName$q $q$testFakeMethodName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our method exit requests were set
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodExitRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testClassName, testMethodName, false),
              (testFakeClassName, testFakeMethodName, true)
            )
          }

          // Remove all method exit requests
          vt.newInputLine(s"mexitclear")

          // Verify our method exit requests were removed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            svm.methodExitRequests should be (empty)
          }
        })
      }
    }
  }
}

