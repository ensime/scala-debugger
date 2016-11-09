package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.tool.frontend.VirtualTerminal
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class MethodEntryListCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("MethodEntryListCommand") {
    it("should list pending and active methodEntrys") {
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
          val lines = Seq(nextLine(vt), nextLine(vt), nextLine(vt)).flatten
          lines should contain allOf(
            s"$testClassName.$testMethodName (Active)\n",
            s"$testFakeClassName.$testFakeMethodName (Pending)\n"
          )
        })
      }
    }
  }
}
