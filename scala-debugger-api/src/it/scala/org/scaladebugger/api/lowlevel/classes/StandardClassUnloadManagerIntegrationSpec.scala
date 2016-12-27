package org.scaladebugger.api.lowlevel.classes

import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class StandardClassUnloadManagerIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("StandardClassUnloadManager") {
    // NOTE: It is not possible to trigger a class unload accurately due to the
    //       JVM's non-deterministic nature with garbage collection.
    ignore("should trigger when a class is unloaded") {
      val testClass = "org.scaladebugger.test.classes.ClassUnload"

      val detectedUnload = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      import s.lowlevel._

      // Mark that we want to receive class unload events and watch for one
      classUnloadManager.createClassUnloadRequest()
      eventManager.addResumingEventHandler(ClassUnloadEventType, _ => {
        detectedUnload.set(true)
      })

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Eventually, we should receive the class unload event
        logTimeTaken(eventually {
          detectedUnload.get() should be (true)
        })
      }
    }
  }
}
