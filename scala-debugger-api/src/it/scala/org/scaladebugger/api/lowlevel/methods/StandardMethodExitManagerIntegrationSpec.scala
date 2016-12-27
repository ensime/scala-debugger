package org.scaladebugger.api.lowlevel.methods

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.{BreakpointEvent, MethodExitEvent}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.events.filters.MethodNameFilter
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class StandardMethodExitManagerIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("StandardMethodExitManager") {
    it("should be able to detect exiting a specific method in a class") {
      val testClass = "org.scaladebugger.test.methods.MethodExit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val expectedClassName =
        "org.scaladebugger.test.methods.MethodExitTestClass"
      val expectedMethodName = "testMethod"
      val methodNameFilter = MethodNameFilter(name = expectedMethodName)

      val leftUnexpectedMethod = new AtomicBoolean(false)
      val leftExpectedMethod = new AtomicBoolean(false)
      val leftMethodAfterLastLine = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Set up the method exit event
      methodExitManager.createMethodExitRequest(
        expectedClassName,
        expectedMethodName
      )

      // Last line in test method
      breakpointManager.createBreakpointRequest(testFile, 28)

      // Listen for breakpoint on first line of method, checking if this
      // breakpoint is hit before or after the method exit event
      eventManager.addResumingEventHandler(BreakpointEventType, e => {
        val breakpointEvent = e.asInstanceOf[BreakpointEvent]
        val location = breakpointEvent.location()
        val fileName = location.sourcePath()
        val lineNumber = location.lineNumber()

        logger.debug(s"Reached breakpoint: $fileName:$lineNumber")

        val methodExitHit = leftExpectedMethod.get()
        leftMethodAfterLastLine.set(!methodExitHit)
      })

      // Listen for method exit events for the specific method
      eventManager.addResumingEventHandler(MethodExitEventType, e => {
        val methodExitEvent = e.asInstanceOf[MethodExitEvent]
        val method = methodExitEvent.method()
        val className = method.declaringType().name()
        val methodName = method.name()

        logger.debug(s"Left method: $className/$methodName")

        if (className == expectedClassName && methodName == expectedMethodName) {
          leftExpectedMethod.set(true)
        } else {
          leftUnexpectedMethod.set(true)
        }
      }, methodNameFilter)


      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          leftUnexpectedMethod.get() should be (false)
          leftExpectedMethod.get() should be (true)
          leftMethodAfterLastLine.get() should be (true)
        })
      }
    }
  }
}
