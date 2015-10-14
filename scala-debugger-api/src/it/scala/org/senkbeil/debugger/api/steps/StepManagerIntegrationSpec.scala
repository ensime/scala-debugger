package org.senkbeil.debugger.api.steps

import java.util.concurrent.atomic.AtomicBoolean
import com.sun.jdi.ThreadReference
import com.sun.jdi.event.{StepEvent, BreakpointEvent}
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.{Timeout, Interval}
import org.scalatest.time.{Units, Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.events.EventType
import org.senkbeil.debugger.api.virtualmachines.ScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}
import EventType._

class StepManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("StepManager") {
    describe("stepping out of") {
      it("should be able to finish executing a method and return to the next line in the parent frame") {
        val testClass = "org.senkbeil.test.steps.MethodCalls"

        // Start on first line of a method
        val startingLine = 15

        // Should return to higher frame on next line
        val expectedLine = 35

        withVirtualMachine(testClass, suspend = false) { (v, s) =>
          verifyStepsFromTo(
            testClass = testClass,
            scalaVirtualMachine = s,
            stepMethod = s.stepManager.stepOut(_: ThreadReference),
            startingLine = startingLine,
            expectedLine = expectedLine
          )
        }
      }

      it("should be able to finish executing a function and return to the next line in the parent frame") {
        val testClass = "org.senkbeil.test.steps.FunctionCalls"

        // Start on first line of a method
        val startingLine = 18

        // Should return to higher frame on next line
        val expectedLine = 41

        withVirtualMachine(testClass, suspend = false) { (v, s) =>
          verifyStepsFromTo(
            testClass = testClass,
            scalaVirtualMachine = s,
            stepMethod = s.stepManager.stepOut(_: ThreadReference),
            startingLine = startingLine,
            expectedLine = expectedLine
          )
        }
      }
    }

    describe("stepping over") {
      it("should skip over each iteration") {
        val testClass = "org.senkbeil.test.steps.BasicIterations"

        // Start on first line of main method
        val startingLine = 13

        // Running through multiple scenarios in this test
        // NOTE: These expectations were made based off of IntelliJ's handling
        val expectedReachableLines = Seq(
          // Can skip entirely over for comprehension
          16,
          // Enters the foreach once (as part of anonymous function declaration?)
          21, 22, 21,
          // Enters the map once (as part of anonymous function declaration?)
          26, 27, 26,
          // Can skip entirely over reduce (why?)
          31,
          // Function object creation
          36,
          // Execution function
          45,
          // Execute method
          47,
          // No-op
          49
        )

        // NOTE: Have to up the maximum duration due to the delay caused by
        //       the for comprehension
        withVirtualMachine(testClass, suspend = false) { (v, s) =>
          verifyStepsOnEach(
            testClass = testClass,
            scalaVirtualMachine = s,
            stepMethod = s.stepManager.stepOver(_: ThreadReference),
            startingLine = startingLine,
            expectedReachableLines = expectedReachableLines,
            failIfNotExact = true,
            maxDuration = (7, Seconds)
          )
        }
      }

      it("should be able to step over declarations and assignments") {
        val testClass = "org.senkbeil.test.steps.BasicAssignments"

        // Start on first line of main method
        val startingLine = 13

        val expectedReachableLines = Seq(14, 16, 18)

        withVirtualMachine(testClass, suspend = false) { (v, s) =>
          verifyStepsOnEach(
            testClass = testClass,
            scalaVirtualMachine = s,
            stepMethod = s.stepManager.stepOver(_: ThreadReference),
            startingLine = startingLine,
            expectedReachableLines = expectedReachableLines
          )
        }
      }

      it("should be able to step back out to higher frame once method finishes") {
        val testClass = "org.senkbeil.test.steps.MethodCalls"

        // Start on last line of a method
        val startingLine = 28

        // Should return to higher frame on next line
        val expectedLine = 39

        withVirtualMachine(testClass, suspend = false) { (v, s) =>
          verifyStepsFromTo(
            testClass = testClass,
            scalaVirtualMachine = s,
            stepMethod = s.stepManager.stepOver(_: ThreadReference),
            startingLine = startingLine,
            expectedLine = expectedLine
          )
        }
      }

      it("should be able to step over all lines in a method") {
        val testClass = "org.senkbeil.test.steps.MethodCalls"

        // Start on first line of main method
        val startingLine = 31

        val expectedReachableLines = Seq(33, 34, 35, 37, 38, 39, 41, 42, 44)

        withVirtualMachine(testClass, suspend = false) { (v, s) =>
          verifyStepsOnEach(
            testClass = testClass,
            scalaVirtualMachine = s,
            stepMethod = s.stepManager.stepOver(_: ThreadReference),
            startingLine = startingLine,
            expectedReachableLines = expectedReachableLines
          )
        }
      }
    }

    describe("stepping into") {
      // TODO: This cannot be done (gets stuck in strings and classloaders)
      //       until we add filtering of Boxed types and Classloaders
      ignore("should enter all iterations except for comprehension") {
        val testClass = "org.senkbeil.test.steps.BasicIterations"

        // Start on first line of main method
        val startingLine = 13

        // Running through multiple scenarios in this test
        // NOTE: These expectations were made based off of IntelliJ's handling
        val expectedReachableLines = Seq(
          /*
           * If prefixed with s, referencing org.senkbeil.test.helpers.Stub…
           *  16, s10, 16,
           *  21, 22, 21, 22, s10, 22, s10, 22, s10, 22, 21,
           *  26, 27, 26, 27, s11, 27, s11, 27, s11, 27, 26,
           *  31, 31, s11, 31, s11, 31, s11, 31, 31,
           *  36,
           *  45, 37, s10, 37, s10, 37, s10, 37,
           *  47, 41, 42, 41, 42, s10, 42, s10, 42, s10, 42, 41,
           *  49, s10, 49
           */
        )

        // NOTE: Have to up the maximum duration due to the delay caused by
        //       the for comprehension
        withVirtualMachine(testClass, suspend = false) { (v, s) =>
          verifyStepsOnEach(
            testClass = testClass,
            scalaVirtualMachine = s,
            stepMethod = s.stepManager.stepInto(_: ThreadReference),
            startingLine = startingLine,
            expectedReachableLines = expectedReachableLines,
            failIfNotExact = true,
            maxDuration = (7, Seconds)
          )
        }
      }

      it("should be able to step into a function in a class") {
        val testClass = "org.senkbeil.test.steps.FunctionCalls"

        val startingLine = 48

        // Should first go to the function definition, then back to the
        // invoking line, and finally to the inside of the function
        val expectedReachableLines = Seq(56, 48, 57)

        withVirtualMachine(testClass, suspend = false) { (v, s) =>
          verifyStepsOnEach(
            testClass = testClass,
            scalaVirtualMachine = s,
            stepMethod = s.stepManager.stepInto(_: ThreadReference),
            startingLine = startingLine,
            expectedReachableLines = expectedReachableLines
          )
        }
      }

      it("should be able to step into a method in a class") {
        val testClass = "org.senkbeil.test.steps.MethodCalls"

        val startingLine = 42
        val expectedLine = 49

        withVirtualMachine(testClass, suspend = false) { (v, s) =>
          verifyStepsFromTo(
            testClass = testClass,
            scalaVirtualMachine = s,
            stepMethod = s.stepManager.stepInto(_: ThreadReference),
            startingLine = startingLine,
            expectedLine = expectedLine
          )
        }
      }
    }
  }

  /**
   * Verifies that the expected line is reached using the provided stepping
   * method. Starts on the starting line provided by setting a breakpoint.
   *
   * @param testClass The full name of the class
   * @param scalaVirtualMachine The Scala virtual machine whose managers to use
   * @param stepMethod The step method to invoke
   * @param startingLine The line in the file to start on
   * @param expectedLine The line in the file to reach
   * @tparam T The return type of the step method
   */
  private def verifyStepsFromTo[T](
    testClass: String,
    scalaVirtualMachine: ScalaVirtualMachine,
    stepMethod: (ThreadReference) => T,
    startingLine: Int,
    expectedLine: Int
  ) = {
    val s = scalaVirtualMachine
    val testFile = scalaClassStringToFileString(testClass)
    // Flag that indicates we reached the expected line
    val success = new AtomicBoolean(false)

    s.breakpointManager.setLineBreakpoint(testFile, startingLine)

    // On receiving a breakpoint, send a step request
    s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
      val breakpointEvent = e.asInstanceOf[BreakpointEvent]
      val className = breakpointEvent.location().declaringType().name()
      val lineNumber = breakpointEvent.location.lineNumber()

      logger.debug(s"Hit breakpoint at $className:$lineNumber")
      stepMethod(breakpointEvent.thread())
    })

    // On receiving a step request, verify that we are in the right
    // location
    s.eventManager.addResumingEventHandler(StepEventType, e => {
      val stepEvent = e.asInstanceOf[StepEvent]
      val className = stepEvent.location().declaringType().name()
      val lineNumber = stepEvent.location().lineNumber()

      logger.debug(s"Stepped onto $className:$lineNumber")
      success.set(lineNumber == expectedLine)
    })

    logTimeTaken(eventually {
      // NOTE: Using asserts to provide more helpful failure messages
      assert(success.get(), s"Did not reach $testClass:$expectedLine!")
    })
  }

  /**
   * Verifies that all lines provided are reached in order using the provided
   * stepping method. Starts on the line provided by setting a breakpoint.
   *
   * @param testClass The full name of the class
   * @param scalaVirtualMachine The Scala virtual machine whose managers to use
   * @param stepMethod The step method to invoke
   * @param startingLine The line in the file to start on
   * @param expectedReachableLines The collection of lines to reach
   * @param failIfNotExact If true, will fail the verification if the lines
   *                       reached do not exactly match the lines provided
   * @param maxDuration The maximum duration (digit, unit) to wait
   * @tparam T The return type of the step method
   */
  private def verifyStepsOnEach[T](
    testClass: String,
    scalaVirtualMachine: ScalaVirtualMachine,
    stepMethod: (ThreadReference) => T,
    startingLine: Int,
    expectedReachableLines: Seq[Int],
    failIfNotExact: Boolean = false,
    maxDuration: (Int, Units) = (5, Seconds)
  ) = {
    val s = scalaVirtualMachine
    val testFile = scalaClassStringToFileString(testClass)
    val expectedLines = collection.mutable.Stack(expectedReachableLines: _*)

    // Used to quit the test early and provide a message
    val failEarly = new AtomicBoolean(false)
    @volatile var failEarlyMessage = "???"

    // Add a breakpoint to get us in the right location for steps
    s.breakpointManager.setLineBreakpoint(testFile, startingLine)

    // On receiving a breakpoint, send a step request
    s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
      val breakpointEvent = e.asInstanceOf[BreakpointEvent]
      val className = breakpointEvent.location().declaringType().name()
      val lineNumber = breakpointEvent.location.lineNumber()

      logger.debug(s"Hit breakpoint at $className:$lineNumber")
      stepMethod(breakpointEvent.thread())
    })

    // On receiving a step request, verify that we are in the right
    // location
    s.eventManager.addResumingEventHandler(StepEventType, e => {
      val stepEvent = e.asInstanceOf[StepEvent]
      val className = stepEvent.location().declaringType().name()
      val lineNumber = stepEvent.location().lineNumber()

      logger.debug(s"Stepped onto $className:$lineNumber")

      val nextLine = expectedLines.top

      // Mark the line as stepped on by removing it (if next in line)
      if (nextLine == lineNumber) {
        expectedLines.pop()

      // Fail the test if we are enforcing strictness and the next line does
      // not match what we expect
      } else if (failIfNotExact) {
        failEarlyMessage =
          s"Line $lineNumber is not the next expected line of $nextLine!"
        failEarly.set(true)
      }

      // Continue stepping if not reached all lines and not exiting early
      if (expectedLines.nonEmpty && !failEarly.get())
        stepMethod(stepEvent.thread())
    })

    // NOTE: Using asserts to provide more helpful failure messages
    logTimeTaken(eventually(
      timeout = Timeout(scaled(Span(maxDuration._1, maxDuration._2))),
      interval = Interval(scaled(Span(5, Milliseconds)))
    ) {
      // If marked to fail early, use that message for better reporting
      assert(!failEarly.get(), failEarlyMessage)

      val stringLines = expectedLines.mkString(",")
      assert(expectedLines.isEmpty,
        s"Did not reach the following lines in order: $stringLines")
    })
  }
}
