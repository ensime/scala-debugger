package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class StepIntoLineCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("StepIntoLineCommand") {
    it("should step into a line successfully") {
      val testClass = "org.scaladebugger.test.steps.MethodCalls"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val threadName = "main"
      val className = s"$testClass$$InnerClass"
      val methodName = "innerMethod"
      val startingLine = 42
      val expectedLine = 49

      // Create a breakpoint to start us off
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q $startingLine")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the starting line via breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:$startingLine\n")
          }

          // Set our active thread
          vt.newInputLine(s"thread $q$threadName$q")

          // Perform our step into
          vt.newInputLine("stepin")

          // Assert we end up where expected
          eventually {
            validateNextLine(
              vt,
              s"Step completed: 'thread=$threadName', $className.$methodName ($testFileName:$expectedLine)\n"
            )
          }
        })
      }
    }
  }
}
