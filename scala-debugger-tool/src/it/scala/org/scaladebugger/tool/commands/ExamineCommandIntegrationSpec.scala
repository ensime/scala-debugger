package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class ExamineCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("ExamineCommand") {
    it("should be able to inspect values (including nested)") {
      val testClass = "org.scaladebugger.test.info.NestedObjects"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val threadName = "main"
      val nestedName = "container.immutableData"

      // Create one breakpoint before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 22")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:22\n")
          }

          // Set our active thread to examine values
          vt.newInputLine(s"thread $q$threadName$q")

          // Request examining a nested value
          vt.newInputLine(s"examine $q$nestedName$q")

          // Gather all data (after delay to allow accumulation of all text)
          val prefix = "-> "
          val waitTime = Constants.AccumulationTimeout.millisPart
          val dataLines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).map(_.stripPrefix(prefix))

          // Validate data is as expected
          dataLines.head should startWith ("immutableData = Instance of")
          dataLines.tail should contain allOf(
            "x = 3",
            "y = \"immutable\""
          )
        })
      }
    }
  }
}
