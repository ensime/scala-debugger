package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class BreakpointListCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("BreakpointListCommand") {
    it("should list pending and active breakpoints") {
      val testClass = "org.scaladebugger.test.breakpoints.DelayedInit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create two breakpoints before connecting to the JVM that are valid
      // and one breakpoint that is not (so always pending)
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 10")
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")
      virtualTerminal.newInputLine("bp \"some/file.scala\" 999")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our breakpoints were set
          validateNextLine(vt, s"Set breakpoint at $testFile:10\n")
          validateNextLine(vt, s"Set breakpoint at $testFile:11\n")
          validateNextLine(vt, "Set breakpoint at some/file.scala:999\n")

          // Verify that we have attached to the JVM
          validateNextLine(vt, "Attached with id",
            success = (text, line) => line should startWith(text))

          // Assert that we hit the first breakpoint
          validateNextLine(vt, s"Breakpoint hit at $testFileName:10\n")

          // List all available breakpoints
          vt.newInputLine("bplist")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Verify expected pending and active breakpoints show up
          // by collecting the three available and checking their content
          val lines = Seq(nextLine(vt), nextLine(vt), nextLine(vt)).flatten
          lines should contain allOf(
            s"$testFile:10 (Active)\n",
            s"$testFile:11 (Active)\n",
            "some/file.scala:999 (Pending)\n"
          )
        })
      }
    }
  }
}
