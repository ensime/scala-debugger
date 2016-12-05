package org.scaladebugger.api.profiles.pure.requests.methods

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import com.sun.jdi.event.{BreakpointEvent, MethodEntryEvent}
import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.events.filters.MethodNameFilter
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureMethodEntryProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureMethodEntryProfile") {
    it("should be able to detect entering a specific method in a class") {
      val testClass = "org.scaladebugger.test.methods.MethodEntry"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val expectedClassName =
        "org.scaladebugger.test.methods.MethodEntryTestClass"
      val expectedMethodName = "testMethod"

      val reachedUnexpectedMethod = new AtomicBoolean(false)
      val reachedExpectedMethod = new AtomicBoolean(false)
      val reachedMethodBeforeFirstLine = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      val methodPipeline = s.withProfile(PureDebugProfile.Name)
        .getOrCreateMethodEntryRequest(expectedClassName, expectedMethodName)
        .map(_.method)
        .map(m => (m.declaringTypeInfo.name, m.name))

      methodPipeline
        .filter(_._1 == expectedClassName)
        .filter(_._2 == expectedMethodName)
        .foreach(_ => reachedExpectedMethod.set(true))

      methodPipeline
        .filterNot(_._1 == expectedClassName)
        .foreach(_ => reachedUnexpectedMethod.set(true))

      methodPipeline
        .filterNot(_._2 == expectedMethodName)
        .foreach(_ => reachedUnexpectedMethod.set(true))

      // First line in test method
      s.getOrCreateBreakpointRequest(testFile, 26)
        .map(_.location)
        .map(l => (l.sourcePath, l.lineNumber))
        .foreach(t => {
        val methodEntryHit = reachedExpectedMethod.get()
        reachedMethodBeforeFirstLine.set(methodEntryHit)
      })

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
