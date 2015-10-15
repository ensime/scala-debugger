package org.senkbeil.debugger.api.jdi.events.filters

import com.sun.jdi.event.BreakpointEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.events.EventType
import org.senkbeil.debugger.api.jdi.requests.properties.CustomProperty
import test.{TestUtilities, VirtualMachineFixtures}
import EventType._

class CustomPropertyFilterIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("CustomPropertyFilter") {
    it("should ignore all events whose custom property does not match the filter") {
      val testClass = "org.senkbeil.debugger.test.filters.CustomPropertyFilter"
      val testFile = scalaClassStringToFileString(testClass)

      // The filter to apply (should ignore any breakpoint event without
      // this property)
      val filter = CustomPropertyFilter(key = "key", value = "value")

      // The property to set on breakpoints to match
      val property = CustomProperty(key = "key", value = "value")

      // Mark lines we want to potentially breakpoint
      val breakpointLines = Seq(9, 10, 11, 13)
      val propertyBreakpoint = 12

      // Expected breakpoints to invoke handler
      val expected = Seq(propertyBreakpoint)

      // Will contain the hit breakpoints
      @volatile var actual = collection.mutable.Seq[Int]()

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Queue up our breakpoints
        breakpointLines.foreach(
          s.breakpointManager.setLineBreakpoint(testFile, _: Int)
        )

        // Set specific breakpoint with custom property
        s.breakpointManager.setLineBreakpoint(
          fileName = testFile,
          lineNumber = propertyBreakpoint,
          extraArguments = Seq(property)
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
