package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.tool.frontend.VirtualTerminal
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class BreakpointClearCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("BreakpointClearCommand") {
    it("should delete a specific pending breakpoint") {
      val testClass = "org.scaladebugger.test.breakpoints.DelayedInit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create a virtual terminal that waits for new input
      val virtualTerminal = newVirtualTerminal()

      // Create two breakpoints before connecting to the JVM that are valid
      // and one breakpoint that is not (so always pending)
      val q = "\""
      virtualTerminal.newInputLine(s"bp $q$testFile$q 10")
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")
      virtualTerminal.newInputLine("bp \"some/file.scala\" 999")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify all breakpoints have been set and that one of
          // them is still pending (no associated file remotely)
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val brs = svm.breakpointRequests
              .map(bri => (bri.fileName, bri.lineNumber, bri.isPending))
            brs should contain theSameElementsAs Seq(
              (testFile, 10, false),
              (testFile, 11, false),
              ("some/file.scala", 999, true)
            )
          }

          // Verify that debugger is running and we have an attached JVM
          eventually {
            sm.state.activeDebugger should not be (None)
            sm.state.scalaVirtualMachines should not be empty
          }

          // Clear a pending breakpoint (some/file.scala:999)
          vt.newInputLine("bpclear \"some/file.scala\" 999")

          // Verify that the deleted breakpoint is gone
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val brs = svm.breakpointRequests
              .map(bri => (bri.fileName, bri.lineNumber, bri.isPending))
            brs should contain theSameElementsAs Seq(
              (testFile, 10, false),
              (testFile, 11, false)
            )
          }
        })
      }
    }

    it("should delete a specific active breakpoint") {
      val testClass = "org.scaladebugger.test.breakpoints.DelayedInit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create a virtual terminal that waits for new input
      val virtualTerminal = newVirtualTerminal()

      // Create two breakpoints before connecting to the JVM that are valid
      // and one breakpoint that is not (so always pending)
      val q = "\""
      virtualTerminal.newInputLine(s"bp $q$testFile$q 10")
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")
      virtualTerminal.newInputLine("bp \"some/file.scala\" 999")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify all breakpoints have been set and that one of
          // them is still pending (no associated file remotely)
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val brs = svm.breakpointRequests
              .map(bri => (bri.fileName, bri.lineNumber, bri.isPending))
            brs should contain theSameElementsAs Seq(
              (testFile, 10, false),
              (testFile, 11, false),
              ("some/file.scala", 999, true)
            )
          }

          // Verify that debugger is running and we have an attached JVM
          eventually {
            sm.state.activeDebugger should not be (None)
            sm.state.scalaVirtualMachines should not be empty
          }

          // Clear an active breakpoint (testFile:999)
          vt.newInputLine(s"bpclear $q$testFile$q 11")

          // Verify that the deleted breakpoint is gone
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val brs = svm.breakpointRequests
              .map(bri => (bri.fileName, bri.lineNumber, bri.isPending))
            brs should contain theSameElementsAs Seq(
              (testFile, 10, false),
              ("some/file.scala", 999, true)
            )
          }
        })
      }
    }

    it("should delete all pending and active breakpoints") {
      val testClass = "org.scaladebugger.test.breakpoints.DelayedInit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create a virtual terminal that waits for new input
      val virtualTerminal = newVirtualTerminal()

      // Create two breakpoints before connecting to the JVM that are valid
      // and one breakpoint that is not (so always pending)
      val q = "\""
      virtualTerminal.newInputLine(s"bp $q$testFile$q 10")
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")
      virtualTerminal.newInputLine("bp \"some/file.scala\" 999")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify all breakpoints have been set and that one of
          // them is still pending (no associated file remotely)
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val brs = svm.breakpointRequests
              .map(bri => (bri.fileName, bri.lineNumber, bri.isPending))
            brs should contain theSameElementsAs Seq(
              (testFile, 10, false),
              (testFile, 11, false),
              ("some/file.scala", 999, true)
            )
          }

          // Verify that debugger is running and we have an attached JVM
          eventually {
            sm.state.activeDebugger should not be (None)
            sm.state.scalaVirtualMachines should not be empty
          }

          // Clear all breakpoints
          vt.newInputLine("bpclear")

          // Verify that all breakpoints are gone
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            svm.breakpointRequests should be (empty)
          }
        })
      }
    }
  }
}
