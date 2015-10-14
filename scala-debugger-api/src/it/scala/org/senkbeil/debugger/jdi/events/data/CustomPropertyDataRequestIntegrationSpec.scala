package org.senkbeil.debugger.jdi.events.data

import com.sun.jdi.event.BreakpointEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.jdi.events.data.requests.CustomPropertyDataRequest
import org.senkbeil.debugger.jdi.events.data.results.CustomPropertyDataResult
import org.senkbeil.debugger.jdi.events.filters.CustomPropertyFilter
import org.senkbeil.debugger.jdi.requests.properties.CustomProperty
import test.{TestUtilities, VirtualMachineFixtures}
import org.senkbeil.debugger.events.EventType._

class CustomPropertyDataRequestIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("CustomPropertyDataRequest") {
    it("should retrieve the custom property if available") {
      val testClass = "org.senkbeil.test.data.CustomPropertyDataRequest"
      val testFile = scalaClassStringToFileString(testClass)

      // The request for data based on a custom property
      val request = CustomPropertyDataRequest(key = "key")

      // The property to set on breakpoints to match
      val property = CustomProperty(key = "key", value = "value")

      // Mark lines we want to potentially breakpoint
      val breakpointLines = Seq(9, 10, 11)
      val propertyBreakpoints = Seq(12, 13)

      // Expected results from data requests
      val expected = Seq(
        CustomPropertyDataResult(key = "key", value = "value"),
        CustomPropertyDataResult(key = "key", value = "value")
      )

      // Will contain the hit breakpoints
      @volatile var actual = collection.mutable.Seq[JDIEventDataResult]()

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Queue up our breakpoints
        breakpointLines.foreach(
          s.breakpointManager.setLineBreakpoint(testFile, _: Int)
        )

        // Set specific breakpoints with custom property
        propertyBreakpoints.foreach(i => s.breakpointManager.setLineBreakpoint(
          fileName = testFile,
          lineNumber = i,
          extraArguments = Seq(property)
        ))

        // Queue up a generic breakpoint event handler that retrieves data
        s.eventManager.addResumingEventHandler(BreakpointEventType, (e, d) => {
          val breakpointEvent = e.asInstanceOf[BreakpointEvent]
          val location = breakpointEvent.location()
          val fileName = location.sourcePath()
          val lineNumber = location.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")
          actual ++= d
        }, request)

        logTimeTaken(eventually {
          actual should contain theSameElementsInOrderAs (expected)
        })
      }
    }
  }
}
