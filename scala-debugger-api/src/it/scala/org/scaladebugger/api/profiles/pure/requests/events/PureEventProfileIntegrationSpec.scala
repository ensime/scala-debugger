package org.scaladebugger.api.profiles.pure.requests.events
import acyclic.file

import java.util.concurrent.atomic.AtomicInteger
import com.sun.jdi.event.BreakpointEvent
import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.lowlevel.events.EventType.BreakpointEventType
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureEventProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureEventProfile") {
    it("should receive events for the specified event type") {
      val testClass = "org.scaladebugger.test.events.LoopingEvent"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val lineNumber1 = 13
      val lineNumber2 = 16
      val lineNumber3 = 19

      val hitLines = collection.mutable.Set[Int]()
      val eventCount = new AtomicInteger(0)

      val s = DummyScalaVirtualMachine.newInstance()

      // Set our breakpoints
      s.withProfile(PureDebugProfile.Name).tryGetOrCreateBreakpointRequest(testFile, lineNumber1)
      s.withProfile(PureDebugProfile.Name).tryGetOrCreateBreakpointRequest(testFile, lineNumber2)
      s.withProfile(PureDebugProfile.Name).tryGetOrCreateBreakpointRequest(testFile, lineNumber3)

      // Set a separate event pipeline to receive events
      s.withProfile(PureDebugProfile.Name)
        .createEventListener(BreakpointEventType)
        .map(_.toBreakpointEvent)
        .map(_.location.lineNumber)
        .foreach(lineNumber => {
          hitLines += lineNumber
          eventCount.incrementAndGet()
        })

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          hitLines should contain allOf (lineNumber1, lineNumber2, lineNumber3)
          eventCount.get() should be > 0
        })
      }
    }

    it("should stop receiving events upon being closed") {
      val testClass = "org.scaladebugger.test.events.LoopingEvent"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val lineNumber1 = 13
      val lineNumber2 = 16
      val lineNumber3 = 19

      val hitLines = collection.mutable.Set[Int]()
      val eventCount = new AtomicInteger(0)

      // TODO: Unable to provide a pending Scala virtual machine as close does
      //       not currently work - update once it does

      withVirtualMachine(testClass) { (s) =>
        // Set our breakpoints
        s.withProfile(PureDebugProfile.Name).tryGetOrCreateBreakpointRequest(testFile, lineNumber1)
        s.withProfile(PureDebugProfile.Name).tryGetOrCreateBreakpointRequest(testFile, lineNumber2)
        s.withProfile(PureDebugProfile.Name).tryGetOrCreateBreakpointRequest(testFile, lineNumber3)

        // Set a separate event pipeline to receive events
        val eventPipeline = s
          .withProfile(PureDebugProfile.Name)
          .createEventListener(BreakpointEventType)

        // Receive events
        eventPipeline
          .map(_.toBreakpointEvent)
          .map(_.location.lineNumber)
          .foreach(lineNumber => {
            hitLines += lineNumber
            eventCount.incrementAndGet()
          })

        logTimeTaken(eventually {
          hitLines should contain allOf(lineNumber1, lineNumber2, lineNumber3)
          eventCount.get() should be > 0
        })

        /* Should have four breakpoint handlers from tryGetOrCreateBreakpointRequest and event */ {
          val currentBreakpointHandlers =
            s.lowlevel.eventManager.getHandlersForEventType(BreakpointEventType)
          currentBreakpointHandlers should have length (4)
        }

        // Stop receiving events
        eventPipeline.close(now = true)
        eventCount.set(0)

        // Should now only have breakpoint handlers for the tryGetOrCreateBreakpointRequest
        // calls and not the raw event pipeline
        {
          val currentBreakpointHandlers =
            s.lowlevel.eventManager.getHandlersForEventType(BreakpointEventType)
          currentBreakpointHandlers should have length (3)
          eventCount.get() should be(0)
        }
      }
    }
  }
}
