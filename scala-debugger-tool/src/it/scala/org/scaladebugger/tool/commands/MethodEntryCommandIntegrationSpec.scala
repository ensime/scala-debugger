package org.scaladebugger.tool.commands
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class MethodEntryCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("MethodEntryCommand") {
    it("should create method entry requests successfully") {
      val testClass = "org.scaladebugger.test.methods.MethodEntry"
      val testClassName = "org.scaladebugger.test.methods.MethodEntryTestClass"
      val testMethodName = "testMethod"

      // Create method entry request before JVM starts
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"mentry $q$testClassName$q $q$testMethodName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our method entry request was set
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodEntryRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testClassName, testMethodName, false)
            )
          }

          // Assert that we hit the method
          eventually {
            validateNextLine(vt, s"Method entry hit for $testClassName.$testMethodName\n")
          }
        })
      }
    }
  }
}

