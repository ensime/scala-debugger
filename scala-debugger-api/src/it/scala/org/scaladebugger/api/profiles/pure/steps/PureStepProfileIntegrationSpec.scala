package org.scaladebugger.api.profiles.pure.steps
import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfoProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.{DummyScalaVirtualMachine, ScalaVirtualMachine}
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.{Interval, Timeout}
import org.scalatest.time.{Milliseconds, Seconds, Span, Units}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.Constants._
import test.{TestUtilities, VirtualMachineFixtures}

class PureStepProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureStepProfile") {
    describe("stepping out of") {
      it("should be able to finish executing a method and return to the next line in the parent frame") {
        val testClass = "org.scaladebugger.test.steps.MethodCalls"

        // Start on first line of a method
        val startingLine = 15

        // Should return to higher frame on next line
        val expectedLine = 35

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsFromTo(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedLine = expectedLine
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepOutLine(_: ThreadInfoProfile))
        }
      }

      it("should be able to finish executing a function and return to the next line in the parent frame") {
        val testClass = "org.scaladebugger.test.steps.FunctionCalls"

        // Start on first line of a method
        val startingLine = 18

        // Should return to higher frame on next line
        val expectedLine = 41

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsFromTo(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedLine = expectedLine
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepOutLine(_: ThreadInfoProfile))
        }
      }
    }

    describe("stepping over") {
      it("should skip over each iteration") {
        val testClass = "org.scaladebugger.test.steps.BasicIterations"

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

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsOnEach(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedReachableLines = expectedReachableLines,
          failIfNotExact = true,
          maxDuration = (15, Seconds)
        )

        // NOTE: Have to up the maximum duration due to the delay caused by
        //       the for comprehension
        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepOverLine(_: ThreadInfoProfile))
        }
      }

      it("should be able to step over declarations and assignments") {
        val testClass = "org.scaladebugger.test.steps.BasicAssignments"

        // Start on first line of main method
        val startingLine = 13

        val expectedReachableLines = Seq(14, 16, 18)

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsOnEach(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedReachableLines = expectedReachableLines
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepOverLine(_: ThreadInfoProfile))
        }
      }

      it("should be able to step back out to higher frame once method finishes") {
        val testClass = "org.scaladebugger.test.steps.MethodCalls"

        // Start on last line of a method
        val startingLine = 28

        // Should return to higher frame on next line
        val expectedLine = 39

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsFromTo(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedLine = expectedLine
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepOverLine(_: ThreadInfoProfile))
        }
      }

      it("should be able to step over all lines in a method") {
        val testClass = "org.scaladebugger.test.steps.MethodCalls"

        // Start on first line of main method
        val startingLine = 31

        val expectedReachableLines = Seq(33, 34, 35, 37, 38, 39, 41, 42, 44)

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsOnEach(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedReachableLines = expectedReachableLines
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepOverLine(_: ThreadInfoProfile))
        }
      }
    }

    describe("stepping into") {
      // TODO: This cannot be done (gets stuck in strings and classloaders)
      //       until we add filtering of Boxed types and Classloaders
      ignore("should enter all iterations except for comprehension") {
        val testClass = "org.scaladebugger.test.steps.BasicIterations"

        // Start on first line of main method
        val startingLine = 13

        // Running through multiple scenarios in this test
        // NOTE: These expectations were made based off of IntelliJ's handling
        val expectedReachableLines = Seq(
          /*
           * If prefixed with s, referencing org.scaladebugger.test.helpers.Stub…
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

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsOnEach(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedReachableLines = expectedReachableLines,
          failIfNotExact = true,
          maxDuration = (15, Seconds)
        )

        // NOTE: Have to up the maximum duration due to the delay caused by
        //       the for comprehension
        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepIntoLine(_: ThreadInfoProfile))
        }
      }

      it("should be able to step into a function in a class") {
        val testClass = "org.scaladebugger.test.steps.FunctionCalls"

        val startingLine = 48

        // Should first go to the function definition, then back to the
        // invoking line, and finally to the inside of the function
        val expectedReachableLines = Seq(56, 48, 57)

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsOnEach(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedReachableLines = expectedReachableLines
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepIntoLine(_: ThreadInfoProfile))
        }
      }

      it("should be able to step into a method in a class") {
        val testClass = "org.scaladebugger.test.steps.MethodCalls"

        val startingLine = 42
        val expectedLine = 49

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsFromTo(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedLine = expectedLine
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepIntoLine(_: ThreadInfoProfile))
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
   * @param startingLine The line in the file to start on
   * @param expectedLine The line in the file to reach
   * @tparam T The return type of the step method
   * @return The function to execute to start the actual verification check
   */
  private def verifyStepsFromTo[T](
    testClass: String,
    scalaVirtualMachine: ScalaVirtualMachine,
    startingLine: Int,
    expectedLine: Int
  ): (ScalaVirtualMachine, () => Unit, (ThreadInfoProfile) => T) => Unit = {
    val testFile = JDITools.scalaClassStringToFileString(testClass)
    // Flag that indicates we reached the expected line
    val success = new AtomicBoolean(false)

    scalaVirtualMachine
      .withProfile(PureDebugProfile.Name)
      .getOrCreateBreakpointRequest(testFile, startingLine)

    // Return a function used to begin the verification
    (s: ScalaVirtualMachine, start: () => Unit, stepMethod: (ThreadInfoProfile) => T) => {
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, startingLine)
        .map(_.thread)
        .foreach(thread => {
          s.withProfile(PureDebugProfile.Name).createStepListener(thread).foreach(stepEvent => {
            val className = stepEvent.location.declaringType.name
            val lineNumber = stepEvent.location.lineNumber

            logger.debug(s"Stepped onto $className:$lineNumber")
            success.set(lineNumber == expectedLine)
          })

          stepMethod(thread)
        })

      start()

      logTimeTaken(eventually {
        // NOTE: Using asserts to provide more helpful failure messages
        assert(success.get(), s"Did not reach $testClass:$expectedLine!")
      })
    }
  }

  /**
   * Verifies that all lines provided are reached in order using the provided
   * stepping method. Starts on the line provided by setting a breakpoint.
   *
   * @param testClass The full name of the class
   * @param scalaVirtualMachine The Scala virtual machine whose managers to use
   * @param startingLine The line in the file to start on
   * @param expectedReachableLines The collection of lines to reach
   * @param failIfNotExact If true, will fail the verification if the lines
   *                       reached do not exactly match the lines provided
   * @param maxDuration The maximum duration (digit, unit) to wait
   * @tparam T The return type of the step method
   * @return The function to execute to start the actual verification check
   */
  private def verifyStepsOnEach[T](
    testClass: String,
    scalaVirtualMachine: ScalaVirtualMachine,
    startingLine: Int,
    expectedReachableLines: Seq[Int],
    failIfNotExact: Boolean = false,
    maxDuration: (Long, Units) = (EventuallyTimeout.toMillis, Milliseconds)
  ): (ScalaVirtualMachine, () => Unit, (ThreadInfoProfile) => T) => Unit = {
    val testFile = JDITools.scalaClassStringToFileString(testClass)
    val expectedLines = collection.mutable.Stack(expectedReachableLines: _*)

    // Used to quit the test early and provide a message
    val failEarly = new AtomicBoolean(false)
    @volatile var failEarlyMessage = "???"

    // Add a breakpoint to get us in the right location for steps
    // On receiving a breakpoint, send a step request
    scalaVirtualMachine
      .withProfile(PureDebugProfile.Name)
      .getOrCreateBreakpointRequest(testFile, startingLine)

    // Return a function used to begin the verification
    (s: ScalaVirtualMachine, start: () => Unit, stepMethod: (ThreadInfoProfile) => T) => {
      // Add a breakpoint to get us in the right location for steps
      // On receiving a breakpoint, send a step request
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, startingLine)
        .map(_.thread)
        .foreach(thread => {
          // On receiving a step request, verify that we are in the right
          // location
          s.withProfile(PureDebugProfile.Name)
            .createStepListener(thread)
            .foreach(stepEvent => {
              val className = stepEvent.location.declaringType.name
              val lineNumber = stepEvent.location.lineNumber

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
              if (expectedLines.nonEmpty && !failEarly.get()) {
                stepMethod(s.thread(stepEvent.thread()))
              }
            })

        stepMethod(thread)
      })

      start()

      // NOTE: Using asserts to provide more helpful failure messages
      logTimeTaken(eventually(
        timeout = Timeout(scaled(Span(maxDuration._1, maxDuration._2))),
        interval = Interval(scaled(test.Constants.EventuallyInterval))
      ) {
        // If marked to fail early, use that message for better reporting
        assert(!failEarly.get(), failEarlyMessage)

        val stringLines = expectedLines.mkString(",")
        assert(expectedLines.isEmpty,
          s"Did not reach the following lines in order: $stringLines")
      })
    }
  }
}
