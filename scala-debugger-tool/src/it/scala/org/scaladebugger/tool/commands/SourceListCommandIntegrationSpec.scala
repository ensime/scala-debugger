package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class SourceListCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("SourceListCommand") {
    it("should print out the source at the current location") {
      val testClass = "test.source.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val threadName = "main"
      val sourcePath = "."

      // Create two breakpoints before connecting to the JVM
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

          // Set active thread
          vt.newInputLine(s"thread $q$threadName$q")

          // Add source path for our file
          vt.newInputLine(s"sourcepath $q$sourcePath$q")

          // Verify that we have finished loading our source files
          eventually {
            validateNextLine(vt, """Loaded \d+ source files""",
              success = (text, line) => line should startWith regex text)
          }

          // Display source for our file
          vt.newInputLine("list")

          // Accumulate source text (delay to allow accumulation of all text)
          val waitTime = ToolConstants.AccumulationTimeout.millisPart
          val lines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim)

          // Verify output represents proper source
          lines should contain theSameElementsInOrderAs Seq(
            "28   \t    val j = List(4, 5, 6)",
            "29   \t    val k = Array(One(\"one\"), 1, true)",
            "30   \t    val l = NullToString",
            "31",
            "32 =>\t    noop(None)",
            "33",
            "34   \t    val m = z1 + c + d + e + f + g",
            "35   \t    val n = i.sum + j.sum",
            "36"
          )
        })
      }
    }

    it("should respect the provided sizing field") {
      val testClass = "test.source.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val threadName = "main"
      val sourcePath = "."
      val testSize = 2

      // Create two breakpoints before connecting to the JVM
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

          // Set active thread
          vt.newInputLine(s"thread $q$threadName$q")

          // Add source path for our file
          vt.newInputLine(s"sourcepath $q$sourcePath$q")

          // Verify that we have finished loading our source files
          eventually {
            validateNextLine(vt, """Loaded \d+ source files""",
              success = (text, line) => line should startWith regex text)
          }

          // Display source for our file
          vt.newInputLine(s"list size=$testSize")

          // Accumulate source text (delay to allow accumulation of all text)
          val waitTime = ToolConstants.AccumulationTimeout.millisPart
          val lines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim)

          // Verify output represents proper source
          lines should contain theSameElementsInOrderAs Seq(
            "30   \t    val l = NullToString",
            "31",
            "32 =>\t    noop(None)",
            "33",
            "34   \t    val m = z1 + c + d + e + f + g"
          )
        })
      }
    }
  }
}
