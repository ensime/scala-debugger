package org.scaladebugger.api.lowlevel.threads

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.ThreadStartEvent
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class StandardThreadStartManagerIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("StandardThreadStartManager") {
    it("should trigger when a thread starts") {
      val testClass = "org.scaladebugger.test.threads.ThreadStart"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val threadStartCount = new AtomicInteger(0)

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Mark that we want to receive thread start events
      threadStartManager.createThreadStartRequest()
      eventManager.addResumingEventHandler(ThreadStartEventType, e => {
        val threadEvent = e.asInstanceOf[ThreadStartEvent]
        val threadName = threadEvent.thread().name()

        logger.debug(s"Detected start of thread named $threadName")
        if (threadName.startsWith("test thread")) {
          logger.trace(s"Thread was desired test thread! Incrementing counter!")
          threadStartCount.incrementAndGet()
        }
      })

      // Start our Thread and listen for the start event
      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Eventually, we should receive a total of 10 thread starts
        logTimeTaken(eventually {
          threadStartCount.get() should be (10)
        })
      }
    }
  }
}
