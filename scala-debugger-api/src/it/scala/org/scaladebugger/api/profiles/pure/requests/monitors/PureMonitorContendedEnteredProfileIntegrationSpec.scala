package org.scaladebugger.api.profiles.pure.requests.monitors

import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureMonitorContendedEnteredProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("PureMonitorContendedEnteredProfile") {
    it("should trigger when a thread enters a monitor after waiting for it to be released by another thread") {
      val testClass = "org.scaladebugger.test.monitors.MonitorContendedEntered"

      val detectedEntered = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive monitor contended entered events and
      // watch for one
      s.withProfile(PureDebugProfile.Name)
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
