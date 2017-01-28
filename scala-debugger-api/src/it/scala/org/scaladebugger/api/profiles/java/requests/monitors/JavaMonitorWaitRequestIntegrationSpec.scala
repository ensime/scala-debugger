package org.scaladebugger.api.profiles.java.requests.monitors

import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class JavaMonitorWaitRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("JavaMonitorWaitRequest") {
    it("should trigger when a thread is about to wait on a monitor object") {
      val testClass = "org.scaladebugger.test.monitors.MonitorWait"

      val detectedWait = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive monitor wait events and
      // watch for one
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateMonitorWaitRequest()
        .foreach(_ => detectedWait.set(true))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Eventually, we should receive the monitor wait event
        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(detectedWait.get(), s"No monitor wait was detected!")
        })
      }
    }
  }
}
