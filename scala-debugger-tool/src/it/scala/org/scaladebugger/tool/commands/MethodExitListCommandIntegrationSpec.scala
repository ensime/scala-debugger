package org.scaladebugger.tool.commands

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class MethodExitListCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("MethodExitListCommand") {
    it("should list pending and active method exit requests") {
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
          validateNextLine(vt, s"Set method exit for class $testClassName and method $testMethodName\n")
          validateNextLine(vt, s"Set method exit for class $testFakeClassName and method $testFakeMethodName\n")

          // Verify that we have attached to the JVM
          validateNextLine(vt, "Attached with id",
            success = (text, line) => line should startWith(text))

          // Assert that we hit the first methodExit
          validateNextLine(vt, s"Method exit hit for $testClassName.$testMethodName\n")

          // List all available method exit requests
          vt.newInputLine("mexitlist")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Verify expected pending and active requests show up
          // by collecting the two available and checking their content
          val lines = Seq(nextLine(vt), nextLine(vt)).flatten
          lines should contain allOf(
            s"$testClassName.$testMethodName (Active)\n",
            s"$testFakeClassName.$testFakeMethodName (Pending)\n"
          )
        })
      }
    }
  }
}
