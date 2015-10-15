package org.senkbeil.debugger.api.breakpoints

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import com.sun.jdi.event.BreakpointEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Milliseconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.events.EventType
import test.{TestUtilities, VirtualMachineFixtures}
import EventType._

class BreakpointManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("BreakpointManager") {
    it("should be able to set breakpoints within while loops") {
      val testClass = "org.senkbeil.debugger.test.breakpoints.WhileLoop"
      val testFile = scalaClassStringToFileString(testClass)

      val firstBreakpointLine = 13
      val firstBreakpointCount = new AtomicInteger(0)

      val secondBreakpointLine = 17
      val secondBreakpointCount = new AtomicInteger(0)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Queue up our breakpoints
        s.breakpointManager.setLineBreakpoint(testFile, firstBreakpointLine)
        s.breakpointManager.setLineBreakpoint(testFile, secondBreakpointLine)

        s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
          val breakpointEvent = e.asInstanceOf[BreakpointEvent]
          val location = breakpointEvent.location()
          val fileName = location.sourcePath()
          val lineNumber = location.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")
          if (fileName == testFile) {
            if (lineNumber == firstBreakpointLine)
              firstBreakpointCount.incrementAndGet()
            if (lineNumber == secondBreakpointLine)
              secondBreakpointCount.incrementAndGet()
          }
        })

        logTimeTaken(eventually {
          firstBreakpointCount.get() should be(10)
          secondBreakpointCount.get() should be(10)
        })
      }
    }

    it("should be able to set breakpoints within for comprehensions") {
      val testClass = "org.senkbeil.debugger.test.breakpoints.ForComprehension"
      val testFile = scalaClassStringToFileString(testClass)

      val firstBreakpointLine = 14
      val firstBreakpointCount = new AtomicInteger(0)

      val secondBreakpointLine = 18
      val secondBreakpointCount = new AtomicInteger(0)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Queue up our breakpoints
        s.breakpointManager.setLineBreakpoint(testFile, firstBreakpointLine)
        s.breakpointManager.setLineBreakpoint(testFile, secondBreakpointLine)

        s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
          val breakpointEvent = e.asInstanceOf[BreakpointEvent]
          val location = breakpointEvent.location()
          val fileName = location.sourcePath()
          val lineNumber = location.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")
          if (fileName == testFile) {
            if (lineNumber == firstBreakpointLine)
              firstBreakpointCount.incrementAndGet()
            if (lineNumber == secondBreakpointLine)
              secondBreakpointCount.incrementAndGet()
          }
        })

        logTimeTaken(eventually {
          firstBreakpointCount.get() should be(10)
          secondBreakpointCount.get() should be(10)
        })
      }
    }

    it("should be able to set breakpoints in a DelayInit object") {
      val testClass = "org.senkbeil.debugger.test.breakpoints.DelayedInit"
      val testFile = scalaClassStringToFileString(testClass)

      val firstBreakpointLine = 10
      val firstBreakpoint = new AtomicBoolean(false)

      val secondBreakpointLine = 11
      val secondBreakpoint = new AtomicBoolean(false)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Queue up our breakpoints
        s.breakpointManager.setLineBreakpoint(testFile, firstBreakpointLine)
        s.breakpointManager.setLineBreakpoint(testFile, secondBreakpointLine)

        s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
          val breakpointEvent = e.asInstanceOf[BreakpointEvent]
          val location = breakpointEvent.location()
          val fileName = location.sourcePath()
          val lineNumber = location.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")
          if (fileName == testFile) {
            if (lineNumber == firstBreakpointLine) firstBreakpoint.set(true)
            if (lineNumber == secondBreakpointLine) secondBreakpoint.set(true)
          }
        })

        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(firstBreakpoint.get(), "First breakpoint not reached!")
          assert(secondBreakpoint.get(), "Second breakpoint not reached!")
        })
      }
    }
  }
}
