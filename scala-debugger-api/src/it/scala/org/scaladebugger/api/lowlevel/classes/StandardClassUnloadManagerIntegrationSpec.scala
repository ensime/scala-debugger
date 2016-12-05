package org.scaladebugger.api.lowlevel.classes

import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class StandardClassUnloadManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

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
