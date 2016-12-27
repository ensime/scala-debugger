package org.scaladebugger.tool.commands

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class MethodExitCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("MethodExitCommand") {
    it("should create method exit requests successfully") {
      val testClass = "org.scaladebugger.test.methods.MethodExit"
      val testClassName = "org.scaladebugger.test.methods.MethodExitTestClass"
      val testMethodName = "testMethod"

      // Create method exit request before JVM starts
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"mexit $q$testClassName$q $q$testMethodName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our method exit request was set
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mers = svm.methodExitRequests
              .map(mei => (mei.className, mei.methodName, mei.isPending))
            mers should contain theSameElementsAs Seq(
              (testClassName, testMethodName, false)
            )
          }

          // Assert that we hit the method
          eventually {
            validateNextLine(vt, s"Method exit hit for $testClassName.$testMethodName\n")
          }
        })
      }
    }
  }
}

