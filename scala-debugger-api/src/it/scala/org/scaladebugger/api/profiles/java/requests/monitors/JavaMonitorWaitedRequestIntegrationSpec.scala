package org.scaladebugger.api.profiles.java.requests.monitors

import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class JavaMonitorWaitedRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("JavaMonitorWaitedRequest") {
    it("should trigger when a thread has finished waiting on a monitor object") {
      val testClass = "org.scaladebugger.test.monitors.MonitorWaited"

      val detectedWaited = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive monitor waited events and
      // watch for one
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateMonitorWaitedRequest()
        .foreach(_ => detectedWaited.set(true))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Eventually, we should receive the monitor waited event
        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(detectedWaited.get(), s"No monitor finished waiting was detected!")
        })
      }
    }
  }
}
