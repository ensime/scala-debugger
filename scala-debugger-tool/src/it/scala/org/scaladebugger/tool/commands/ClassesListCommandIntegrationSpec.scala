package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class ClassesListCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("ClassesListCommand") {
    it("should list all classes if no arguments provided") {
      val testClass = "org.scaladebugger.test.classes.ClassPrepare"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create a breakpoint before connecting to JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the first breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:11\n")
          }

          // List all available classes
          vt.newInputLine("classes")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Gather all classes after (delay to allow accumulation of all text)
          val prefix = "-> "
          val waitTime = ToolConstants.AccumulationTimeout.millisPart
          val classLines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).map(_.drop(prefix.length))
            .filter(_.contains("org.scaladebugger"))

          // Verify our expected classes show up
          classLines should contain allOf(
            "org.scaladebugger.test.classes.ClassPrepare",
            "org.scaladebugger.test.classes.CustomClass"
          )
        })
      }
    }

    it("should limit to classes matching filter") {
      val testClass = "org.scaladebugger.test.classes.ClassPrepare"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create a breakpoint before connecting to JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the first breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:11\n")
          }

          // List all available classes
          vt.newInputLine("classes filter=\"*.CustomClass\"")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Gather all classes after (delay to allow accumulation of all text)
          val prefix = "-> "
          val waitTime = ToolConstants.AccumulationTimeout.millisPart
          val classLines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).map(_.drop(prefix.length))
            .filter(_.contains("org.scaladebugger"))

          // Verify our expected classes show up
          classLines should contain ("org.scaladebugger.test.classes.CustomClass")

          // Verify our unexpected classes do not show up
          classLines should not contain ("org.scaladebugger.test.classes.ClassPrepare")
        })
      }
    }

    it("should limit to classes not matching filternot") {
      val testClass = "org.scaladebugger.test.classes.ClassPrepare"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create a breakpoint before connecting to JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the first breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:11\n")
          }

          // List all available classes
          vt.newInputLine("classes filternot=\"*.CustomClass\"")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Gather all classes after (delay to allow accumulation of all text)
          val prefix = "-> "
          val waitTime = ToolConstants.AccumulationTimeout.millisPart
          val classLines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).map(_.drop(prefix.length))
            .filter(_.contains("org.scaladebugger"))

          // Verify our expected classes show up
          classLines should contain ("org.scaladebugger.test.classes.ClassPrepare")

          // Verify our unexpected classes do not show up
          classLines should not contain ("org.scaladebugger.test.classes.CustomClass")
        })
      }
    }
  }
}
