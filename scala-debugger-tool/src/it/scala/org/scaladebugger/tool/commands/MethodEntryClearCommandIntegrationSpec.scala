package org.scaladebugger.tool.commands

import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class MethodEntryClearCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("MethodEntryClearCommand") {
    it("should delete a specific pending method entry request") {
      val testClass = "org.scaladebugger.test.methods.MethodEntry"
      val testClassName = "org.scaladebugger.test.methods.MethodEntryTestClass"
      val testMethodName = "testMethod"

      val testFakeClassName = "invalid.class"
      val testFakeMethodName = "fakeMethod"

      // Create method entry request before JVM starts
      val q = "\""
      val virtualTerminal = newVirtualTerminal()

      // Valid, so will be active
      virtualTerminal.newInputLine(s"mentry $q$testClassName$q $q$testMethodName$q")

      // Invalid, so will be inactive
      virtualTerminal.newInputLine(s"mentry $q$testFakeClassName$q $q$testFakeMethodName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our method entry requests were set
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodEntryRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testClassName, testMethodName, false),
              (testFakeClassName, testFakeMethodName, true)
            )
          }

          // Remove the pending method entry request
          vt.newInputLine(s"mentryclear $q$testFakeClassName$q $q$testFakeMethodName$q")

          // Verify our pending method entry request was removed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodEntryRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testClassName, testMethodName, false)
            )
          }
        })
      }
    }

    it("should delete a specific active method entry request") {
      val testClass = "org.scaladebugger.test.methods.MethodEntry"
      val testClassName = "org.scaladebugger.test.methods.MethodEntryTestClass"
      val testMethodName = "testMethod"

      val testFakeClassName = "invalid.class"
      val testFakeMethodName = "fakeMethod"

      // Create method entry requests before JVM starts
      val q = "\""
      val virtualTerminal = newVirtualTerminal()

      // Valid, so will be active
      virtualTerminal.newInputLine(s"mentry $q$testClassName$q $q$testMethodName$q")

      // Invalid, so will be inactive
      virtualTerminal.newInputLine(s"mentry $q$testFakeClassName$q $q$testFakeMethodName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our method entry requests were set
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodEntryRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testClassName, testMethodName, false),
              (testFakeClassName, testFakeMethodName, true)
            )
          }

          // Remove the active method entry request
          vt.newInputLine(s"mentryclear $q$testClassName$q $q$testMethodName$q")

          // Verify our active method entry request was removed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodEntryRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testFakeClassName, testFakeMethodName, true)
            )
          }
        })
      }
    }

    it("should delete all method entry requests for a specific class") {
      val testClass = "org.scaladebugger.test.methods.MethodEntry"
      val testClassName = "org.scaladebugger.test.methods.MethodEntryTestClass"
      val testMethodName = "testMethod"

      val testFakeClassName = "invalid.class"
      val testFakeMethodName = "fakeMethod"

      // Create method entry requests before JVM starts
      val q = "\""
      val virtualTerminal = newVirtualTerminal()

      // Valid, so will be active
      virtualTerminal.newInputLine(s"mentry $q$testClassName$q $q$testMethodName$q")

      // Invalid, so will be inactive
      virtualTerminal.newInputLine(s"mentry $q$testFakeClassName$q $q$testFakeMethodName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our method entry requests were set
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodEntryRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testClassName, testMethodName, false),
              (testFakeClassName, testFakeMethodName, true)
            )
          }

          // Remove all method entry requests for a class
          vt.newInputLine(s"mentryclear $q$testClassName$q")

          // Verify our method entry requests were removed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            svm.methodEntryRequests should be (empty)
          }
        })
      }
    }

    // TODO: All method entry requests become active, none are staying pending
    // This might have been intentional... maybe I don't get feedback about
    // invalid method entry requests? How do I get feedback about breakpoints?
    it("should delete all method entry requests") {
      val testClass = "org.scaladebugger.test.methods.MethodEntry"
      val testClassName = "org.scaladebugger.test.methods.MethodEntryTestClass"
      val testMethodName = "testMethod"

      val testFakeClassName = "invalid.class"
      val testFakeMethodName = "fakeMethod"

      // Create method entry requests before JVM starts
      val q = "\""
      val virtualTerminal = newVirtualTerminal()

      // Valid, so will be active
      virtualTerminal.newInputLine(s"mentry $q$testClassName$q $q$testMethodName$q")

      // Invalid, so will be inactive
      virtualTerminal.newInputLine(s"mentry $q$testFakeClassName$q $q$testFakeMethodName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our method entry requests were set
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodEntryRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testClassName, testMethodName, false),
              (testFakeClassName, testFakeMethodName, true)
            )
          }

          // Remove all method entry requests
          vt.newInputLine(s"mentryclear")

          // Verify our method entry requests were removed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            svm.methodEntryRequests should be (empty)
          }
        })
      }
    }
  }
}

