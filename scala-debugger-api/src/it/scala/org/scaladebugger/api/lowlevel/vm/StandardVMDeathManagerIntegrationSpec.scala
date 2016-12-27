package org.scaladebugger.api.lowlevel.vm
import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiConstants, ApiTestUtilities, VirtualMachineFixtures}

class StandardVMDeathManagerIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("StandardVMDeathManager") {
    it("should trigger when a virtual machine dies", ApiConstants.NoWindows) {
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
