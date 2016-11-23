package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class FieldsListCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("FieldsListCommand") {
    it("should list all classes if no arguments provided") {
      val testClass = "org.scaladebugger.test.info.Fields"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName
      val testClassName = "org.scaladebugger.test.info.Fields$TestCaseClass"

      // Create a breakpoint before connecting to JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 71")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the first breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:71\n")
          }

          // List all available fields
          vt.newInputLine(s"fields $q$testClassName$q")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Gather all fields after (delay to allow accumulation of all text)
          val prefix = "-> "
          val fieldLines = Stream.continually(vt.nextOutputLine(waitTime = 500))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).map(_.drop(prefix.length))

          // Verify our expected classes show up
          fieldLines should contain allOf(
            "primitive: int",
            "obj: java.lang.String"
          )
        })
      }
    }

    it("should limit to classes matching filter") {
      val testClass = "org.scaladebugger.test.info.Fields"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val testClassName = "org.scaladebugger.test.info.Fields$TestCaseClass"
      val testFieldName = "primitive"

      // Create a breakpoint before connecting to JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 71")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the first breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:71\n")
          }

          // List all available fields
          vt.newInputLine(s"fields $q$testClassName$q filter=$q$testFieldName$q")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Gather all fields after (delay to allow accumulation of all text)
          val prefix = "-> "
          val fieldLines = Stream.continually(vt.nextOutputLine(waitTime = 500))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).map(_.drop(prefix.length))

          // Verify our expected fields show up
          fieldLines should contain ("primitive: int")

          // Verify our unexpected fields do not show up
          fieldLines should not contain ("obj: java.lang.String")
        })
      }
    }

    it("should limit to classes not matching filternot") {
      val testClass = "org.scaladebugger.test.info.Fields"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val testClassName = "org.scaladebugger.test.info.Fields$TestCaseClass"
      val testFieldName = "primitive"

      // Create a breakpoint before connecting to JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 71")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the first breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:71\n")
          }

          // List all available fields
          vt.newInputLine(s"fields $q$testClassName$q filternot=$q$testFieldName$q")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Gather all fields after (delay to allow accumulation of all text)
          val prefix = "-> "
          val fieldLines = Stream.continually(vt.nextOutputLine(waitTime = 500))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).map(_.drop(prefix.length))

          // Verify our expected fields show up
          fieldLines should contain ("obj: java.lang.String")

          // Verify our unexpected fields do not show up
          fieldLines should not contain ("primitive: int")
        })
      }
    }
  }
}
