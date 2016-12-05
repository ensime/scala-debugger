package org.scaladebugger.api.lowlevel.monitors

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.MonitorContendedEnterEvent
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class StandardMonitorContendedEnterManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("StandardMonitorContendedEnterManager") {
    it("should trigger when a thread attempts to enter a monitor already acquired by another thread") {
      val testClass = "org.scaladebugger.test.monitors.MonitorContendedEnter"

      val detectedEnter = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Mark that we want to receive monitor contended enter events and
      // watch for one
      monitorContendedEnterManager.createMonitorContendedEnterRequest()
      eventManager.addResumingEventHandler(MonitorContendedEnterEventType, e => {
        val monitorContendedEnterEvent =
          e.asInstanceOf[MonitorContendedEnterEvent]

        val threadName = monitorContendedEnterEvent.thread().name()
        val monitorTypeName =
          monitorContendedEnterEvent.monitor().referenceType().name()

        logger.debug(s"Detected attempted monitor enter in thread $threadName for monitor of type $monitorTypeName")
        detectedEnter.set(true)
      })

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Eventually, we should receive the monitor contended enter event
        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(detectedEnter.get(), s"No monitor enter attempt was detected!")
        })
      }
    }
  }
}
