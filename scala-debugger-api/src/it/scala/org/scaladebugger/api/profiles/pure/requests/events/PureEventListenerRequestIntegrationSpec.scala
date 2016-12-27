package org.scaladebugger.api.profiles.pure.requests.events

import java.util.concurrent.atomic.AtomicInteger

import org.scaladebugger.api.lowlevel.events.EventType.BreakpointEventType
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class PureEventListenerRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("PureEventListenerRequest") {
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
