package org.scaladebugger.api.lowlevel.monitors

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.MonitorWaitedEvent
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class StandardMonitorWaitedManagerIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("StandardMonitorWaitedManager") {
    it("should trigger when a thread has finished waiting on a monitor object") {
      val testClass = "org.scaladebugger.test.monitors.MonitorWaited"

      val detectedWaited = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Mark that we want to receive monitor waited events and
      // watch for one
      monitorWaitedManager.createMonitorWaitedRequest()
      eventManager.addResumingEventHandler(MonitorWaitedEventType, e => {
        val monitorWaitedEvent =
          e.asInstanceOf[MonitorWaitedEvent]

        val threadName = monitorWaitedEvent.thread().name()
        val monitorTypeName =
          monitorWaitedEvent.monitor().referenceType().name()

        logger.debug(s"Detected finished waiting in thread $threadName for monitor of type $monitorTypeName")
        detectedWaited.set(true)
      })

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Eventually, we should receive the monitor waited event
        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(detectedWaited.get(), s"No monitor finished waiting was detected!")
        })
      }
    }
  }
}
