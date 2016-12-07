package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class MethodsListCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("MethodsListCommand") {
    it("should list all methods if no arguments provided") {
      val testClass = "org.scaladebugger.test.info.Methods"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName
      val testClassName = "org.scaladebugger.test.info.Methods$"

      // Create a breakpoint before connecting to JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 22")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the first breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:22\n")
          }

          // List all available methods
          vt.newInputLine(s"methods $q$testClassName$q")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Gather all methods after (delay to allow accumulation of all text)
          val prefix = "-> "
          val waitTime = Constants.AccumulationTimeout.millisPart
          val methodLines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).map(_.drop(prefix.length))

          // Verify our expected classes show up
          methodLines should contain allOf(
            "publicMethod(int,java.lang.String)",
            "zeroArgMethod()"
          )
        })
      }
    }

    it("should limit to methods matching filter") {
      val testClass = "org.scaladebugger.test.info.Methods"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val testClassName = "org.scaladebugger.test.info.Methods$"
      val testMethodName = "publicMethod"

      // Create a breakpoint before connecting to JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 22")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the first breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:22\n")
          }

          // List all available methods
          vt.newInputLine(s"methods $q$testClassName$q filter=$q$testMethodName$q")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Gather all methods after (delay to allow accumulation of all text)
          val prefix = "-> "
          val waitTime = Constants.AccumulationTimeout.millisPart
          val methodLines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).map(_.drop(prefix.length))

          // Verify our expected methods show up
          methodLines should contain ("publicMethod(int,java.lang.String)")

          // Verify our unexpected methods do not show up
          methodLines should not contain ("zeroArgMethod()")
        })
      }
    }

    it("should limit to methods not matching filternot") {
      val testClass = "org.scaladebugger.test.info.Methods"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      val testClassName = "org.scaladebugger.test.info.Methods$"
      val testMethodName = "publicMethod"

      // Create a breakpoint before connecting to JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 22")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Assert that we hit the first breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:22\n")
          }

          // List all available methods
          vt.newInputLine(s"methods $q$testClassName$q filternot=$q$testMethodName$q")

          // First prints out JVM id
          validateNextLine(vt, "JVM",
            success = (text, line) => line should include(text))

          // Gather all methods after (delay to allow accumulation of all text)
          val prefix = "-> "
          val waitTime = Constants.AccumulationTimeout.millisPart
          val methodLines = Stream.continually(vt.nextOutputLine(waitTime = waitTime))
            .takeWhile(_.nonEmpty).flatten.map(_.trim).map(_.drop(prefix.length))

          // Verify our expected methods show up
          methodLines should contain ("zeroArgMethod()")

          // Verify our unexpected methods do not show up
          methodLines should not contain ("publicMethod(int,java.lang.String")
        })
      }
    }
  }
}
