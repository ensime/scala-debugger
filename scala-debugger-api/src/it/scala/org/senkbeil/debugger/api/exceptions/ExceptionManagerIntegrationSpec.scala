package org.senkbeil.debugger.api.exceptions

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import com.sun.jdi.event.{BreakpointEvent, ClassPrepareEvent, ExceptionEvent}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.events.EventType._
import test.{TestUtilities, VirtualMachineFixtures}

class ExceptionManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("ExceptionManager") {
    it("should be able to detect exceptions in try blocks") {
      val testClass =
        "org.senkbeil.debugger.test.exceptions.InsideTryBlockException"
      val testFile = scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.senkbeil.debugger.test.exceptions.CustomException"

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Use a breakpoint prior to our exceptions to prepare without passing
        // the exceptions
        s.breakpointManager.setLineBreakpoint(testFile, 10)

        // When breakpoint triggered, assume the exception class has been loaded
        s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
          val breakpointEvent = e.asInstanceOf[BreakpointEvent]
          val location = breakpointEvent.location()
          val fileName = location.sourcePath()
          val lineNumber = location.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")

          // Mark the exception we want to watch (now that the class
          // is available)
          s.exceptionManager.setException(
            exceptionName = expectedExceptionName,
            notifyCaught = true,
            notifyUncaught = false
          )

          s.eventManager.addResumingEventHandler(ExceptionEventType, e => {
            val exceptionEvent = e.asInstanceOf[ExceptionEvent]
            val exceptionName = exceptionEvent.exception().referenceType().name()

            logger.debug(s"Detected exception: $exceptionName")
            if (exceptionName == expectedExceptionName)
              detectedException.set(true)
          })
        })

        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }

    it("should be able to detect exceptions in functional try calls") {
      val testClass =
        "org.senkbeil.debugger.test.exceptions.InsideFunctionalTryException"
      val testFile = scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.senkbeil.debugger.test.exceptions.CustomException"

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Use a breakpoint prior to our exceptions to prepare without passing
        // the exceptions
        s.breakpointManager.setLineBreakpoint(testFile, 10)

        // When breakpoint triggered, assume the exception class has been loaded
        s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
          val breakpointEvent = e.asInstanceOf[BreakpointEvent]
          val location = breakpointEvent.location()
          val fileName = location.sourcePath()
          val lineNumber = location.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")

          // Mark the exception we want to watch (now that the class
          // is available)
          s.exceptionManager.setException(
            exceptionName = expectedExceptionName,
            notifyCaught = true,
            notifyUncaught = false
          )

          s.eventManager.addResumingEventHandler(ExceptionEventType, e => {
            val exceptionEvent = e.asInstanceOf[ExceptionEvent]
            val exceptionName = exceptionEvent.exception().referenceType().name()

            logger.debug(s"Detected exception: $exceptionName")
            if (exceptionName == expectedExceptionName)
              detectedException.set(true)
          })
        })

        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }

    it("should be able to detect exceptions outside of try blocks") {
      val testClass =
        "org.senkbeil.debugger.test.exceptions.OutsideTryException"
      val testFile = scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.senkbeil.debugger.test.exceptions.CustomException"

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Use a breakpoint prior to our exceptions to prepare without passing
        // the exceptions
        s.breakpointManager.setLineBreakpoint(testFile, 10)

        // When breakpoint triggered, assume the exception class has been loaded
        s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
          val breakpointEvent = e.asInstanceOf[BreakpointEvent]
          val location = breakpointEvent.location()
          val fileName = location.sourcePath()
          val lineNumber = location.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")

          // Mark the exception we want to watch (now that the class
          // is available)
          s.exceptionManager.setException(
            exceptionName = expectedExceptionName,
            notifyCaught = false,
            notifyUncaught = true
          )

          s.eventManager.addResumingEventHandler(ExceptionEventType, e => {
            val exceptionEvent = e.asInstanceOf[ExceptionEvent]
            val exceptionName = exceptionEvent.exception().referenceType().name()

            logger.debug(s"Detected exception: $exceptionName")
            if (exceptionName == expectedExceptionName)
              detectedException.set(true)
          })
        })

        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }
  }
}
