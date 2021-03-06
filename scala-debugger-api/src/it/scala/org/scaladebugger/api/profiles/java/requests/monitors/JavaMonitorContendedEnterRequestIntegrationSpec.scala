package org.scaladebugger.api.profiles.java.requests.monitors

import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class JavaMonitorContendedEnterRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("JavaMonitorContendedEnterRequest") {
    it("should trigger when a thread attempts to enter a monitor already acquired by another thread") {
      val testClass = "org.scaladebugger.test.monitors.MonitorContendedEnter"

      val detectedEnter = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive monitor contended enter events and
      // watch for one
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateMonitorContendedEnterRequest()
        .foreach(_ => detectedEnter.set(true))

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
