package org.scaladebugger.api.profiles.pure.requests.vm

import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class PureVMStartRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("PureVMStartRequest") {
    it("should trigger when a virtual machine starts") {
      val testClass = "org.scaladebugger.test.misc.MainUsingApp"

      val detectedStart = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      s.withProfile(PureDebugProfile.Name)
        .getOrCreateVMStartRequest()
        .foreach(_ => detectedStart.set(true))

      // Start our VM and listen for the start event
      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Eventually, we should receive the start event
        logTimeTaken(eventually {
          detectedStart.get() should be (true)
        })
      }
    }
  }
}
