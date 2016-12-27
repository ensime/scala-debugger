package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class ThreadListCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("ThreadListCommand") {
    it("should limit list of threads to a specific thread group if provided") {
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

          // List all threads
          vt.newInputLine(s"threads $q$threadGroupName$q")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Accumulate other text (delay to allow accumulation of all text)
          val waitTime = ToolConstants.AccumulationTimeout.millisPart
          val lines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).mkString("\n")

          // Verify that we have various information included
          lines should include ("Group main")
          lines should not include ("Group system")
          lines should include ("main Running suspended at breakpoint")
        })
      }
    }

    it("should list all thread of the active JVMs") {
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

          // List all threads
          vt.newInputLine("threads")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Accumulate other text (delay to allow accumulation of all text)
          val waitTime = ToolConstants.AccumulationTimeout.millisPart
          val lines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).mkString("\n")

          // Verify that we have various information included
          lines should include ("Group main")
          lines should include ("Group system")
          lines should include ("main Running suspended at breakpoint")
        })
      }
    }
  }
}
