package org.scaladebugger.tool.commands

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class MethodEntryListCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("MethodEntryListCommand") {
    it("should list pending and active method entry requests") {
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
          validateNextLine(vt, s"Set method entry for class $testClassName and method $testMethodName\n")
          validateNextLine(vt, s"Set method entry for class $testFakeClassName and method $testFakeMethodName\n")

          // Verify that we have attached to the JVM
          validateNextLine(vt, "Attached with id",
            success = (text, line) => line should startWith(text))

          // Assert that we hit the first methodEntry
          validateNextLine(vt, s"Method entry hit for $testClassName.$testMethodName\n")

          // List all available method entry requests
          vt.newInputLine("mentrylist")

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
