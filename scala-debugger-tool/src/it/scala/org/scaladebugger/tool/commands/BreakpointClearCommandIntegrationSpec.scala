package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.tool.frontend.VirtualTerminal
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, TestUtilities, ToolFixtures}

class BreakpointClearCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("BreakpointClearCommand") {
    it("should delete a specific pending breakpoint") {
      val testClass = "org.scaladebugger.test.breakpoints.DelayedInit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create two breakpoints before connecting to the JVM that are valid
      // and one breakpoint that is not (so always pending)
      val q = "\""
      val virtualTerminal = new VirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 10")
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")
      virtualTerminal.newInputLine("bp \"some/file.scala\" 999")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        // Construct our "nextLine" method based on average timeout
        val waitTime = Constants.NextOutputLineTimeout.millisPart
        val nextLineOpt = () => vt.nextOutputLine(waitTime = waitTime)
        val nextLine = () => nextLineOpt().getOrElse(
          throw new Exception("Next line input timed out!"))

        logTimeTaken({
          // Verify our breakpoints were set
          nextLine() should be (s"Set breakpoint at $testFile:10\n")
          nextLine() should be (s"Set breakpoint at $testFile:11\n")
          nextLine() should be ("Set breakpoint at some/file.scala:999\n")

          // Verify that we have attached to the JVM
          nextLine() should startWith ("Attached with id")

          // Assert that we hit the first breakpoint
          nextLine() should be (s"Breakpoint hit at $testFileName:10\n")

          // Clear a pending breakpoint (some/file.scala:999)
          vt.newInputLine("bpclear \"some/file.scala\" 999")

          // Verify action was performed
          nextLine() should be ("Cleared breakpoint at some/file.scala:999\n")

          // List all available breakpoints
          vt.newInputLine("bplist")

          // First prints out JVM id
          nextLine() should include ("JVM")

          // Verify expected pending and active breakpoints show up
          // by collecting the three available and checking their content
          val lines = Seq(nextLineOpt(), nextLineOpt(), nextLineOpt()).flatten
          lines should contain allOf(
            s"$testFile:10\n",
            s"$testFile:11\n"
          )
        })
      }
    }

    it("should delete a specific active breakpoint") {
      val testClass = "org.scaladebugger.test.breakpoints.DelayedInit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create two breakpoints before connecting to the JVM that are valid
      // and one breakpoint that is not (so always pending)
      val q = "\""
      val virtualTerminal = new VirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 10")
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")
      virtualTerminal.newInputLine("bp \"some/file.scala\" 999")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        // Construct our "nextLine" method based on average timeout
        val waitTime = Constants.NextOutputLineTimeout.millisPart
        val nextLineOpt = () => vt.nextOutputLine(waitTime = waitTime)
        val nextLine = () => nextLineOpt().getOrElse(
          throw new Exception("Next line input timed out!"))

        logTimeTaken({
          // Verify our breakpoints were set
          nextLine() should be (s"Set breakpoint at $testFile:10\n")
          nextLine() should be (s"Set breakpoint at $testFile:11\n")
          nextLine() should be ("Set breakpoint at some/file.scala:999\n")

          // Verify that we have attached to the JVM
          nextLine() should startWith ("Attached with id")

          // Assert that we hit the first breakpoint
          nextLine() should be (s"Breakpoint hit at $testFileName:10\n")

          // Clear an active breakpoint (testFile:11)
          vt.newInputLine(s"bpclear $q$testFile$q 11")

          // Verify action was performed
          nextLine() should be (s"Cleared breakpoint at $testFile:11\n")

          // List all available breakpoints
          vt.newInputLine("bplist")

          // First prints out JVM id
          nextLine() should include ("JVM")

          // Verify expected pending and active breakpoints show up
          // by collecting the three available and checking their content
          val lines = Seq(nextLineOpt(), nextLineOpt(), nextLineOpt()).flatten
          lines should contain allOf(
            s"$testFile:10\n",
            "some/file.scala:999\n"
          )
        })
      }
    }

    it("should delete all pending and active breakpoints") {
      val testClass = "org.scaladebugger.test.breakpoints.DelayedInit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testFileName = new File(testFile).getName

      // Create two breakpoints before connecting to the JVM that are valid
      // and one breakpoint that is not (so always pending)
      val q = "\""
      val virtualTerminal = new VirtualTerminal()
      virtualTerminal.newInputLine(s"bp $q$testFile$q 10")
      virtualTerminal.newInputLine(s"bp $q$testFile$q 11")
      virtualTerminal.newInputLine("bp \"some/file.scala\" 999")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        // Construct our "nextLine" method based on average timeout
        val waitTime = Constants.NextOutputLineTimeout.millisPart
        val nextLineOpt = () => vt.nextOutputLine(waitTime = waitTime)
        val nextLine = () => nextLineOpt().getOrElse(
          throw new Exception("Next line input timed out!"))

        logTimeTaken({
          // Verify our breakpoints were set
          nextLine() should be (s"Set breakpoint at $testFile:10\n")
          nextLine() should be (s"Set breakpoint at $testFile:11\n")
          nextLine() should be ("Set breakpoint at some/file.scala:999\n")

          // Verify that we have attached to the JVM
          nextLine() should startWith ("Attached with id")

          // Assert that we hit the first breakpoint
          nextLine() should be (s"Breakpoint hit at $testFileName:10\n")

          // Clear all breakpoints
          vt.newInputLine("bpclear")

          // Verify action was performed
          nextLine() should be ("Cleared all breakpoints\n")

          // List all available breakpoints
          vt.newInputLine("bplist")

          // First prints out JVM id
          nextLine() should include ("JVM")

          // Nothing else should be printed
          nextLineOpt() should be (None)
        })
      }
    }
  }
}
