package org.senkbeil.debugger.api.methods

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.{BreakpointEvent, MethodExitEvent}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.events.EventType._
import org.senkbeil.debugger.api.jdi.events.filters.MethodNameFilter
import test.{TestUtilities, VirtualMachineFixtures}

class MethodExitManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("MethodExitManager") {
    it("should be able to detect exiting a specific method in a class") {
      val testClass = "org.senkbeil.debugger.test.methods.MethodExit"
      val testFile = scalaClassStringToFileString(testClass)

      val expectedClassName =
        "org.senkbeil.debugger.test.methods.MethodExitTestClass"
      val expectedMethodName = "testMethod"
      val methodNameFilter = MethodNameFilter(name = expectedMethodName)

      val leftUnexpectedMethod = new AtomicBoolean(false)
      val leftExpectedMethod = new AtomicBoolean(false)
      val leftMethodAfterLastLine = new AtomicBoolean(false)

      withVirtualMachine(testClass, suspend = false) { (v, s) =>
        // Set up the method exit event
        s.methodExitManager.setMethodExit(
          expectedClassName,
          expectedMethodName
        )

        // Last line in test method
        s.breakpointManager.setLineBreakpoint(testFile, 28)

        // Listen for breakpoint on first line of method, checking if this
        // breakpoint is hit before or after the method exit event
        s.eventManager.addResumingEventHandler(BreakpointEventType, e => {
          val breakpointEvent = e.asInstanceOf[BreakpointEvent]
          val location = breakpointEvent.location()
          val fileName = location.sourcePath()
          val lineNumber = location.lineNumber()

          logger.debug(s"Reached breakpoint: $fileName:$lineNumber")

          val methodExitHit = leftExpectedMethod.get()
          leftMethodAfterLastLine.set(!methodExitHit)
        })

        // Listen for method exit events for the specific method
        s.eventManager.addResumingEventHandler(MethodExitEventType, e => {
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

        logTimeTaken(eventually {
          leftUnexpectedMethod.get() should be (false)
          leftExpectedMethod.get() should be (true)
          leftMethodAfterLastLine.get() should be (true)
        })
      }
    }
  }
}
