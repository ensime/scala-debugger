package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.profiles.traits.info.ThreadGroupInfo
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class ThreadGroupCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("ThreadGroupCommand") {
    it("should clear the active thread group if no name provided") {
      val testClass = "org.scaladebugger.test.breakpoints.DelayedInit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create a breakpoint before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 10")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:10\n")
          }

          // Set a fake active thread group
          sm.updateActiveThreadGroup(mock[ThreadGroupInfo])

          // Clear our thread group
          vt.newInputLine(s"threadgroup")

          // Verify that the active thread group is empty
          eventually {
            sm.state.activeThreadGroup should be (None)
          }
        })
      }
    }

    it("should set the active thread group by name if provided") {
      val testClass = "org.scaladebugger.test.breakpoints.DelayedInit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val threadGroupName = "main"

      // Create a breakpoint before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 10")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:10\n")
          }

          // Set our active thread group
          vt.newInputLine(s"threadgroup $q$threadGroupName$q")

          // Verify that the active thread group is set
          eventually {
            sm.state.activeThreadGroup.get.name should be (threadGroupName)
          }
        })
      }
    }
  }
}
