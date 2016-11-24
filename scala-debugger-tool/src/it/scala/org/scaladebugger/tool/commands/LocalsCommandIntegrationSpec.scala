package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class LocalsCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("LocalsCommand") {
    it("should be able to list fields and local variables") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val threadName = "main"

      // Create one breakpoint before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 44")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:44\n")
          }

          // Set our active thread to examine values
          vt.newInputLine(s"thread $q$threadName$q")

          // Request list of local variables and fields
          vt.newInputLine("locals")

          // Gather all data (after delay to allow accumulation of all text)
          val prefix = "-> "
          val waitTime = Constants.AccumulationTimeout.millisPart
          val lines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).map(_.stripPrefix(prefix))

          // NOTE: Not taking exhaustive list because
          //       a) we are not validating the logic, just the output
          //       b) some output is harder to test, such as objects
          //          with unique ids, without breaking the contain allOf
          lines should contain allOf(
            "[FIELDS]",
            "z1 = 1",
            "z2 = \"something\"",
            "z3 = null",
            "[LOCALS]",
            "a = true",
            "b = 'c'",
            "c = 3",
            "d = 4",
            "e = 5",
            "f = 1.0",
            "g = 2.0"
          )
        })
      }
    }
  }
}
