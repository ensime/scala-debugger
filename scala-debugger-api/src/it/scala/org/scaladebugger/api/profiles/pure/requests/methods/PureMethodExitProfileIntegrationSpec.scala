package org.scaladebugger.api.profiles.pure.requests.methods

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import com.sun.jdi.event.{BreakpointEvent, MethodExitEvent}
import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.events.filters.MethodNameFilter
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureMethodExitProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("PureMethodExitProfile") {
    it("should be able to detect exiting a specific method in a class") {
      val testClass = "org.scaladebugger.test.methods.MethodExit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val expectedClassName =
        "org.scaladebugger.test.methods.MethodExitTestClass"
      val expectedMethodName = "testMethod"

      val leftUnexpectedMethod = new AtomicBoolean(false)
      val leftExpectedMethod = new AtomicBoolean(false)
      val leftMethodAfterLastLine = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      val methodPipeline = s.withProfile(PureDebugProfile.Name)
        .getOrCreateMethodExitRequest(expectedClassName, expectedMethodName)
        .map(_.method)
        .map(m => (m.declaringTypeInfo.name, m.name))

      methodPipeline
        .filter(_._1 == expectedClassName)
        .filter(_._2 == expectedMethodName)
        .foreach(_ => leftExpectedMethod.set(true))

      methodPipeline
        .filterNot(_._1 == expectedClassName)
        .foreach(_ => leftUnexpectedMethod.set(true))

      methodPipeline
        .filterNot(_._2 == expectedMethodName)
        .foreach(_ => leftUnexpectedMethod.set(true))

      // Last line in test method
      s.getOrCreateBreakpointRequest(testFile, 28)
        .map(_.location)
        .map(l => (l.sourcePath, l.lineNumber))
        .foreach(t => {
        val methodExitHit = leftExpectedMethod.get()
        leftMethodAfterLastLine.set(!methodExitHit)
      })

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
