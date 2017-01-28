package org.scaladebugger.api.profiles.java.requests.monitors

import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class JavaMonitorContendedEnteredRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("JavaMonitorContendedEnteredRequest") {
    it("should trigger when a thread enters a monitor after waiting for it to be released by another thread") {
      val testClass = "org.scaladebugger.test.monitors.MonitorContendedEntered"

      val detectedEntered = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive monitor contended entered events and
      // watch for one
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateMonitorContendedEnteredRequest()
        .foreach(_ => detectedEntered.set(true))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Eventually, we should receive the monitor contended entered event
        logTimeTaken(eventually {
          // NOTE: Using asserts to provide more helpful failure messages
          assert(detectedEntered.get(), s"No monitor entered was detected!")
        })
      }
    }
  }
}
