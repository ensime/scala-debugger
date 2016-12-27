package org.scaladebugger.api.lowlevel.threads

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.ThreadDeathEvent
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class StandardThreadDeathManagerIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("StandardThreadDeathManager") {
    it("should trigger when a thread dies") {
      val testClass = "org.scaladebugger.test.threads.ThreadDeath"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val threadDeathCount = new AtomicInteger(0)

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Mark that we want to receive thread death events
      threadDeathManager.createThreadDeathRequest()
      eventManager.addResumingEventHandler(ThreadDeathEventType, e => {
        val threadEvent = e.asInstanceOf[ThreadDeathEvent]
        val threadName = threadEvent.thread().name()

        logger.debug(s"Detected death of thread named $threadName")
        if (threadName.startsWith("test thread")) {
          logger.trace(s"Thread was desired test thread! Incrementing counter!")
          threadDeathCount.incrementAndGet()
        }
      })

      // Start our Thread and listen for the start event
      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Eventually, we should receive a total of 10 thread deaths
        logTimeTaken(eventually {
          threadDeathCount.get() should be (10)
        })
      }
    }
  }
}
