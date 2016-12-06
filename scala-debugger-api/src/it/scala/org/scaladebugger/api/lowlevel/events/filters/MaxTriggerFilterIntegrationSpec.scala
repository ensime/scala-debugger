package org.scaladebugger.api.lowlevel.events.filters

import com.sun.jdi.event.BreakpointEvent
import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventType
import EventType._
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class MaxTriggerFilterIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("MaxTriggerFilter") {
    it("should ignore all events for a handler after the first N using MaxTriggerFilter(N)") {
      val testClass = "org.scaladebugger.test.filters.MaxTriggerFilter"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      // The filter to apply (should ignore breakpoints after first three)
      val filter = MaxTriggerFilter(count = 3)

      // Mark lines we want to potentially breakpoint
      val breakpointLines = Seq(9, 10, 11, 12, 13)

      // Expected breakpoints to invoke handler
      val expected = Seq(9, 10, 11)

      // Will contain the hit breakpoints
      @volatile var actual = collection.mutable.Seq[Int]()

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Queue up our breakpoints
      breakpointLines.foreach(
        breakpointManager.createBreakpointRequest(testFile, _: Int)
      )

      // Queue up a generic breakpoint event handler that filters events
      eventManager.addResumingEventHandler(BreakpointEventType, e => {
        val breakpointEvent = e.asInstanceOf[BreakpointEvent]
        val location = breakpointEvent.location()
        val fileName = location.sourcePath()
        val lineNumber = location.lineNumber()

        logger.debug(s"Reached breakpoint: $fileName:$lineNumber")
        actual :+= lineNumber
      }, filter)

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          actual should contain theSameElementsInOrderAs (expected)
        })
      }
    }
  }
}
