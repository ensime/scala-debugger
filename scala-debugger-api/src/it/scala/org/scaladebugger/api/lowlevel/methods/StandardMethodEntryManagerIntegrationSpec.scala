package org.scaladebugger.api.lowlevel.methods

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.{BreakpointEvent, MethodEntryEvent}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.events.filters.MethodNameFilter
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class StandardMethodEntryManagerIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("StandardMethodEntryManager") {
    it("should be able to detect entering a specific method in a class") {
      val testClass = "org.scaladebugger.test.methods.MethodEntry"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val expectedClassName =
        "org.scaladebugger.test.methods.MethodEntryTestClass"
      val expectedMethodName = "testMethod"
      val methodNameFilter = MethodNameFilter(name = expectedMethodName)

      val reachedUnexpectedMethod = new AtomicBoolean(false)
      val reachedExpectedMethod = new AtomicBoolean(false)
      val reachedMethodBeforeFirstLine = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Set up the method entry event
      methodEntryManager.createMethodEntryRequest(
        expectedClassName,
        expectedMethodName
      )

      // First line in test method
      breakpointManager.createBreakpointRequest(testFile, 26)

      // Listen for breakpoint on first line of method, checking if this
      // breakpoint is hit before or after the method entry event
      eventManager.addResumingEventHandler(BreakpointEventType, e => {
        val breakpointEvent = e.asInstanceOf[BreakpointEvent]
        val location = breakpointEvent.location()
        val fileName = location.sourcePath()
        val lineNumber = location.lineNumber()

        logger.debug(s"Reached breakpoint: $fileName:$lineNumber")

        val methodEntryHit = reachedExpectedMethod.get()
        reachedMethodBeforeFirstLine.set(methodEntryHit)
      })

      // Listen for method entry events for the specific method
      eventManager.addResumingEventHandler(MethodEntryEventType, e => {
        val methodEntryEvent = e.asInstanceOf[MethodEntryEvent]
        val method = methodEntryEvent.method()
        val className = method.declaringType().name()
        val methodName = method.name()

        logger.debug(s"Reached method: $className/$methodName")

        if (className == expectedClassName && methodName == expectedMethodName) {
          reachedExpectedMethod.set(true)
        } else {
          reachedUnexpectedMethod.set(true)
        }
      }, methodNameFilter)

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          reachedUnexpectedMethod.get() should be (false)
          reachedExpectedMethod.get() should be (true)
          reachedMethodBeforeFirstLine.get() should be (true)
        })
      }
    }
  }
}
