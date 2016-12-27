package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class BreakpointCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("BreakpointCommand") {
    it("should create breakpoints successfully") {
      val testClass = "org.scaladebugger.test.breakpoints.DelayedInit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create two breakpoints before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 10")
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our breakpoints were set
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val brs = svm.breakpointRequests
              .map(bri => (bri.fileName, bri.lineNumber, bri.isPending))
            brs should contain theSameElementsAs Seq(
              (testFile, 10, false),
              (testFile, 11, false)
            )
          }

          // Assert that we hit the first breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:10\n")
          }

          // Continue on to the next breakpoint (resume main thread)
          vt.newInputLine("resume \"main\"")

          // Assert that we hit the second breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:11\n")
          }
        })
      }
    }
  }
}
