package org.senkbeil.debugger.api.jdi.events.filters

import com.sun.jdi.event.BreakpointEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.events.EventType
import test.{TestUtilities, VirtualMachineFixtures}
import EventType._

class MinTriggerFilterIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("MinTriggerFilter") {
    it("should ignore the first N events for a handler using MinTriggerFilter(N)") {
      val testClass = "org.senkbeil.test.filters.MinTriggerFilter"
      val testFile = scalaClassStringToFileString(testClass)

      // The filter to apply (should ignore the first three breakpoints)
      val filter = MinTriggerFilter(count = 3)

      // Mark lines we want to potentially breakpoint
      val breakpointLines = Seq(9, 10, 11, 12, 13)

      // Expected breakpoints to invoke handler
      val expected = Seq(12, 13)

      // Will contain the hit breakpoints
      @volatile var actual = collection.mutable.Seq[Int]()

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Queue up our breakpoints
        breakpointLines.foreach(
          s.breakpointManager.setLineBreakpoint(testFile, _: Int)
        )

        // Queue up a generic breakpoint event handler that filters events
        s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
          val breakpointEvent = e.asInstanceOf[BreakpointEvent]
          val location = breakpointEvent.location()
          val fileName = location.sourcePath()
          val lineNumber = location.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")
          actual :+= lineNumber
        }, filter)

        logTimeTaken(eventually {
          actual should contain theSameElementsInOrderAs (expected)
        })
      }
    }
  }
}
