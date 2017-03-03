package org.scaladebugger.api.profiles.java.requests.vm
import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiConstants, ApiTestUtilities, VirtualMachineFixtures}

class JavaVMDeathRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("JavaVMDeathRequest") {
    it("should trigger when a virtual machine dies", ApiConstants.NoWindows) {
      val testClass = "org.scaladebugger.test.misc.MainUsingApp"

      val detectedDeath = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive vm death events and watch for one
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateVMDeathRequest()
        .foreach(_ => detectedDeath.set(true))

      // Start our VM and listen for the start event
      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Kill the JVM process so we get a disconnect event
        // NOTE: This does not seem to trigger the VMDeathEvent on Windows
        s.underlyingVirtualMachine.process().destroy()

        // Eventually, we should receive the start event
        logTimeTaken(eventually {
          detectedDeath.get() should be (true)
        })
      }
    }
  }
}
