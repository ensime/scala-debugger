package org.scaladebugger.api.lowlevel.breakpoints

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import com.sun.jdi.event.BreakpointEvent
import org.scaladebugger.api.lowlevel.events.EventType
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class StandardBreakpointManagerIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("StandardBreakpointManager") {
    it("should be able to set breakpoints within while loops") {
      val testClass = "org.scaladebugger.test.breakpoints.WhileLoop"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val firstBreakpointLine = 13
      val firstBreakpointCount = new AtomicInteger(0)

      val secondBreakpointLine = 17
      val secondBreakpointCount = new AtomicInteger(0)

      val s = DummyScalaVirtualMachine.newInstance()

      import s.lowlevel._

      // Queue up our breakpoints
      breakpointManager.createBreakpointRequest(testFile, firstBreakpointLine)
      breakpointManager.createBreakpointRequest(testFile, secondBreakpointLine)

      eventManager.addResumingEventHandler(BreakpointEventType, e => {
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

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          firstBreakpointCount.get() should be(10)
          secondBreakpointCount.get() should be(10)
        })
      }
    }

    it("should be able to set breakpoints within for comprehensions") {
      val testClass = "org.scaladebugger.test.breakpoints.ForComprehension"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val firstBreakpointLine = 14
      val firstBreakpointCount = new AtomicInteger(0)

      val secondBreakpointLine = 18
      val secondBreakpointCount = new AtomicInteger(0)

      val s = DummyScalaVirtualMachine.newInstance()

      import s.lowlevel._

      // Queue up our breakpoints
      breakpointManager.createBreakpointRequest(testFile, firstBreakpointLine)
      breakpointManager.createBreakpointRequest(testFile, secondBreakpointLine)

      eventManager.addResumingEventHandler(BreakpointEventType, e => {
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

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          firstBreakpointCount.get() should be(10)
          secondBreakpointCount.get() should be(10)
        })
      }
    }

    it("should be able to set breakpoints in a DelayInit object") {
      val testClass = "org.scaladebugger.test.breakpoints.DelayedInit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val firstBreakpointLine = 10
      val firstBreakpoint = new AtomicBoolean(false)

      val secondBreakpointLine = 11
      val secondBreakpoint = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      import s.lowlevel._

      // Queue up our breakpoints
      breakpointManager.createBreakpointRequest(testFile, firstBreakpointLine)
      breakpointManager.createBreakpointRequest(testFile, secondBreakpointLine)

      eventManager.addResumingEventHandler(BreakpointEventType, e => {
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

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(firstBreakpoint.get(), "First breakpoint not reached!")
          assert(secondBreakpoint.get(), "Second breakpoint not reached!")
        })
      }
    }
  }
}
