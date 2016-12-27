package test

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.ThreadReference
import com.sun.jdi.event.{BreakpointEvent, StepEvent}
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.utils.{JDITools, Logging}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.{Interval, Timeout}
import org.scalatest.time.{Milliseconds, Span, Units}
import org.scaladebugger.api.lowlevel.events.EventType._

/**
 * Contains helper methods for testing.
 */
trait ApiTestUtilities extends Eventually { this: Logging =>
  import test.ApiConstants.EventuallyTimeout
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.ApiConstants.EventuallyTimeout),
    interval = scaled(test.ApiConstants.EventuallyInterval)
  )

  /**
   * Executes the block of code and logs the time taken to evaluate it.
   *
   * @param block The block of code to execute
   * @tparam T The return type of the block of code
   * @return The value returned from the block of code
   */
  def logTimeTaken[T](block: => T): T = {
    val startTime = System.currentTimeMillis()

    try {
      block
    } finally {
      val finalTime = System.currentTimeMillis() - startTime
      logger.info(s"Time taken: ${finalTime / 1000.0}s")
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
  def lowlevelVerifyStepsFromTo[T](
    testClass: String,
    scalaVirtualMachine: ScalaVirtualMachine,
    startingLine: Int,
    expectedLine: Int
  ): (ScalaVirtualMachine, () => Unit, (ThreadReference) => T) => Unit = {
    val testFile = JDITools.scalaClassStringToFileString(testClass)
    // Flag that indicates we reached the expected line
    val success = new AtomicBoolean(false)

    scalaVirtualMachine.lowlevel.breakpointManager
      .createBreakpointRequest(testFile, startingLine)

    // Return a function used to begin the verification
    (s: ScalaVirtualMachine, start: () => Unit, stepMethod: (ThreadReference) => T) => {
      import s.lowlevel._

      // On receiving a breakpoint, send a step request
      eventManager.addResumingEventHandler(BreakpointEventType, e => {
        val breakpointEvent = e.asInstanceOf[BreakpointEvent]
        val className = breakpointEvent.location().declaringType().name()
        val lineNumber = breakpointEvent.location.lineNumber()

        logger.debug(s"Hit breakpoint at $className:$lineNumber")
        stepMethod(breakpointEvent.thread())
      })

      // On receiving a step request, verify that we are in the right
      // location
      eventManager.addResumingEventHandler(StepEventType, e => {
        val stepEvent = e.asInstanceOf[StepEvent]
        val className = stepEvent.location().declaringType().name()
        val lineNumber = stepEvent.location().lineNumber()

        logger.debug(s"Stepped onto $className:$lineNumber")
        success.set(lineNumber == expectedLine)
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
  def lowlevelVerifyStepsOnEach[T](
    testClass: String,
    scalaVirtualMachine: ScalaVirtualMachine,
    startingLine: Int,
    expectedReachableLines: Seq[Int],
    failIfNotExact: Boolean = false,
    maxDuration: (Long, Units) = (EventuallyTimeout.toMillis, Milliseconds)
  ): (ScalaVirtualMachine, () => Unit, (ThreadReference) => T) => Unit = {
    val testFile = JDITools.scalaClassStringToFileString(testClass)
    val expectedLines = collection.mutable.Stack(expectedReachableLines: _*)

    // Used to quit the test early and provide a message
    val failEarly = new AtomicBoolean(false)
    @volatile var failEarlyMessage = "???"

    // Add a breakpoint to get us in the right location for steps
    scalaVirtualMachine.lowlevel.breakpointManager
      .createBreakpointRequest(testFile, startingLine)

    // Return a function used to begin the verification
    (s: ScalaVirtualMachine, start: () => Unit, stepMethod: (ThreadReference) => T) => {
      import s.lowlevel._

      // On receiving a breakpoint, send a step request
      eventManager.addResumingEventHandler(BreakpointEventType, e => {
        val breakpointEvent = e.asInstanceOf[BreakpointEvent]
        val className = breakpointEvent.location().declaringType().name()
        val lineNumber = breakpointEvent.location.lineNumber()

        logger.debug(s"Hit breakpoint at $className:$lineNumber")
        stepMethod(breakpointEvent.thread())
      })

      // On receiving a step request, verify that we are in the right
      // location
      eventManager.addResumingEventHandler(StepEventType, e => {
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

      start()

      // NOTE: Using asserts to provide more helpful failure messages
      logTimeTaken(eventually(
        timeout = Timeout(scaled(Span(maxDuration._1, maxDuration._2))),
        interval = Interval(scaled(test.ApiConstants.EventuallyInterval))
      ) {
        // If marked to fail early, use that message for better reporting
        assert(!failEarly.get(), failEarlyMessage)

        val stringLines = expectedLines.mkString(",")
        assert(expectedLines.isEmpty,
          s"Did not reach the following lines in order: $stringLines")
      })
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
  def verifyStepsFromTo[T](
    testClass: String,
    scalaVirtualMachine: ScalaVirtualMachine,
    startingLine: Int,
    expectedLine: Int
  ): (ScalaVirtualMachine, () => Unit, (ThreadInfo) => T) => Unit = {
    val testFile = JDITools.scalaClassStringToFileString(testClass)
    // Flag that indicates we reached the expected line
    val success = new AtomicBoolean(false)

    scalaVirtualMachine
      .withProfile(PureDebugProfile.Name)
      .getOrCreateBreakpointRequest(testFile, startingLine)

    // Return a function used to begin the verification
    (s: ScalaVirtualMachine, start: () => Unit, stepMethod: (ThreadInfo) => T) => {
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
  def verifyStepsOnEach[T](
    testClass: String,
    scalaVirtualMachine: ScalaVirtualMachine,
    startingLine: Int,
    expectedReachableLines: Seq[Int],
    failIfNotExact: Boolean = false,
    maxDuration: (Long, Units) = (EventuallyTimeout.toMillis, Milliseconds)
  ): (ScalaVirtualMachine, () => Unit, (ThreadInfo) => T) => Unit = {
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
    (s: ScalaVirtualMachine, start: () => Unit, stepMethod: (ThreadInfo) => T) => {
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
                stepMethod(stepEvent.thread)
              }
            })

          stepMethod(thread)
        })

      start()

      // NOTE: Using asserts to provide more helpful failure messages
      logTimeTaken(eventually(
        timeout = Timeout(scaled(Span(maxDuration._1, maxDuration._2))),
        interval = Interval(scaled(test.ApiConstants.EventuallyInterval))
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
