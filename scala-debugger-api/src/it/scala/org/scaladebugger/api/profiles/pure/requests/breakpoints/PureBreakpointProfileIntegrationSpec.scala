package org.scaladebugger.api.profiles.pure.requests.breakpoints

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import com.sun.jdi.event.BreakpointEvent
import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.breakpoints.PendingBreakpointSupport
import org.scaladebugger.api.lowlevel.events.EventType
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureBreakpointProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("PureBreakpointProfile") {
    it("should be able to set breakpoints within while loops") {
      val testClass = "org.scaladebugger.test.breakpoints.WhileLoop"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val firstBreakpointLine = 13
      val firstBreakpointCount = new AtomicInteger(0)

      val secondBreakpointLine = 17
      val secondBreakpointCount = new AtomicInteger(0)

      val s = DummyScalaVirtualMachine.newInstance()
      s.withProfile(PureDebugProfile.Name)
        .tryGetOrCreateBreakpointRequest(testFile, firstBreakpointLine)
        .get
        .map(_.location)
        .map(l => (l.sourcePath, l.lineNumber))
        .filter(_._1 == testFile)
        .filter(_._2 == firstBreakpointLine)
        .foreach(_ => firstBreakpointCount.incrementAndGet())

      s.withProfile(PureDebugProfile.Name)
        .tryGetOrCreateBreakpointRequest(testFile, secondBreakpointLine)
        .get
        .map(_.location)
        .map(l => (l.sourcePath, l.lineNumber))
        .filter(_._1 == testFile)
        .filter(_._2 == secondBreakpointLine)
        .foreach(_ => secondBreakpointCount.incrementAndGet())

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          firstBreakpointCount.get() should be(10)
          secondBreakpointCount.get() should be(10)
        })
      }
    }

    it("should be able to set breakpoints within for comprehensions") {
      val testClass = "org.scaladebugger.test.breakpoints.ForComprehension"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val firstBreakpointLine = 14
      val firstBreakpointCount = new AtomicInteger(0)

      val secondBreakpointLine = 18
      val secondBreakpointCount = new AtomicInteger(0)

      val s = DummyScalaVirtualMachine.newInstance()
      s.withProfile(PureDebugProfile.Name)
        .tryGetOrCreateBreakpointRequest(testFile, firstBreakpointLine)
        .get
        .map(_.location)
        .map(l => (l.sourcePath, l.lineNumber))
        .filter(_._1 == testFile)
        .filter(_._2 == firstBreakpointLine)
        .foreach(_ => firstBreakpointCount.incrementAndGet())

      s.withProfile(PureDebugProfile.Name)
        .tryGetOrCreateBreakpointRequest(testFile, secondBreakpointLine)
        .get
        .map(_.location)
        .map(l => (l.sourcePath, l.lineNumber))
        .filter(_._1 == testFile)
        .filter(_._2 == secondBreakpointLine)
        .foreach(_ => secondBreakpointCount.incrementAndGet())

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          firstBreakpointCount.get() should be(10)
          secondBreakpointCount.get() should be(10)
        })
      }
    }

    it("should be able to set breakpoints in a DelayInit object") {
      val testClass = "org.scaladebugger.test.breakpoints.DelayedInit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val firstBreakpointLine = 10
      val firstBreakpoint = new AtomicBoolean(false)

      val secondBreakpointLine = 11
      val secondBreakpoint = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, firstBreakpointLine)
        .map(_.location)
        .map(l => (l.sourcePath, l.lineNumber))
        .filter(_._1 == testFile)
        .filter(_._2 == firstBreakpointLine)
        .foreach(_ => firstBreakpoint.set(true))

      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, secondBreakpointLine)
        .map(_.location)
        .map(l => (l.sourcePath, l.lineNumber))
        .filter(_._1 == testFile)
        .filter(_._2 == secondBreakpointLine)
        .foreach(_ => secondBreakpoint.set(true))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(firstBreakpoint.get(), "First breakpoint not reached!")
          assert(secondBreakpoint.get(), "Second breakpoint not reached!")
        })
      }
    }
  }
}
