package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class SetCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("SetCommand") {
    it("should be able to set a non-float value") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val threadName = "main"
      val variableName = "z1"
      val newValue = 33

      // Create one breakpoint before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 32")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:32\n")
          }

          // Set our active thread to examine values
          vt.newInputLine(s"thread $q$threadName$q")

          // Request examining a nested value
          vt.newInputLine(s"set $q$variableName$q $newValue")

          // Verify the value was changed
          vt.newInputLine(s"examine $q$variableName$q")

          // Value should be reported as changed
          validateNextLine(vt, s"$variableName = $newValue\n")
        })
      }
    }

    it("should be able to set a local variable value") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val threadName = "main"
      val variableName = "g"
      val newValue = 33.5

      // Create one breakpoint before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 32")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:32\n")
          }

          // Set our active thread to examine values
          vt.newInputLine(s"thread $q$threadName$q")

          // Request examining a nested value
          vt.newInputLine(s"set $q$variableName$q $newValue")

          // Verify the value was changed
          vt.newInputLine(s"examine $q$variableName$q")

          // Value should be reported as changed
          validateNextLine(vt, s"$variableName = $newValue\n")
        })
      }
    }

    it("should be able to set a field value") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val threadName = "main"
      val variableName = "z2"
      val newValue = "something new"

      // Create one breakpoint before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 32")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:32\n")
          }

          // Set our active thread to examine values
          vt.newInputLine(s"thread $q$threadName$q")

          // Request examining a nested value
          vt.newInputLine(s"set $q$variableName$q $q$newValue$q")

          // Verify the value was changed
          vt.newInputLine(s"examine $q$variableName$q")

          // Value should be reported as changed
          validateNextLine(vt, s"$variableName = $q$newValue$q\n")
        })
      }
    }
  }
}
