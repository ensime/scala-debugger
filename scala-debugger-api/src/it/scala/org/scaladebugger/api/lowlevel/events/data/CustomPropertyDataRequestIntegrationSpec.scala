package org.scaladebugger.api.lowlevel.events.data

import com.sun.jdi.event.BreakpointEvent
import org.scaladebugger.api.lowlevel.events.EventType
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.events.data.requests.CustomPropertyDataRequest
import org.scaladebugger.api.lowlevel.events.data.results.CustomPropertyDataResult
import org.scaladebugger.api.lowlevel.requests.properties.CustomProperty
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class CustomPropertyDataRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("CustomPropertyDataRequest") {
    it("should retrieve the custom property if available") {
      val testClass =
        "org.scaladebugger.test.data.CustomPropertyDataRequest"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

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

      val s = DummyScalaVirtualMachine.newInstance()

      import s.lowlevel._

      // Queue up our breakpoints
      breakpointLines.foreach(
        breakpointManager.createBreakpointRequest(testFile, _: Int)
      )

      // Set specific breakpoints with custom property
      propertyBreakpoints.foreach(i => breakpointManager.createBreakpointRequest(
        fileName = testFile,
        lineNumber = i,
        property
      ))

      // Queue up a generic breakpoint event handler that retrieves data
      eventManager.addResumingEventHandler(BreakpointEventType, (e, d) => {
        val breakpointEvent = e.asInstanceOf[BreakpointEvent]
        val location = breakpointEvent.location()
        val fileName = location.sourcePath()
        val lineNumber = location.lineNumber()

        logger.debug(s"Reached breakpoint: $fileName:$lineNumber")
        actual ++= d
      }, request)

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          actual should contain theSameElementsInOrderAs (expected)
        })
      }
    }
  }
}
