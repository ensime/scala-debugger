package org.scaladebugger.api.lowlevel.vm
import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{Constants, TestUtilities, VirtualMachineFixtures}
import org.scaladebugger.api.lowlevel.events.EventType._

class StandardVMDeathManagerIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("StandardVMDeathManager") {
    it("should trigger when a virtual machine dies", Constants.NoWindows) {
      val testClass = "org.scaladebugger.test.misc.MainUsingApp"

      val detectedDeath = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Mark that we want to receive vm death events and watch for one
      vmDeathManager.createVMDeathRequest()
      eventManager.addResumingEventHandler(VMDeathEventType, _ => {
        detectedDeath.set(true)
      })

      // Start our VM and listen for the start event
      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Kill the JVM process so we get a disconnect event
        // NOTE: This does not seem to trigger the VMDeathEvent on Windows
        s.underlyingVirtualMachine.process().destroy()

        // Eventually, we should receive the death event
        logTimeTaken(eventually {
          detectedDeath.get() should be (true)
        })
      }
    }
  }
}
