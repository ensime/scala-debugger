package org.scaladebugger.api.lowlevel.events.filters

import com.sun.jdi.event.BreakpointEvent
import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventType
import org.scaladebugger.api.lowlevel.requests.properties.CustomProperty
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}
import EventType._

class CustomPropertyFilterIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("CustomPropertyFilter") {
    it("should ignore all events whose custom property does not match the filter") {
      val testClass = "org.scaladebugger.test.filters.CustomPropertyFilter"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

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

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Queue up our breakpoints
      breakpointLines.foreach(
        breakpointManager.createBreakpointRequest(testFile, _: Int)
      )

      // Set specific breakpoint with custom property
      breakpointManager.createBreakpointRequest(
        fileName = testFile,
        lineNumber = propertyBreakpoint,
        property
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
