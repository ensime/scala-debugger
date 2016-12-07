package org.scaladebugger.api.profiles.pure.requests.monitors

import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureMonitorWaitRequestIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("PureMonitorWaitRequest") {
    it("should trigger when a thread is about to wait on a monitor object") {
      val testClass = "org.scaladebugger.test.monitors.MonitorWait"

      val detectedWait = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive monitor wait events and
      // watch for one
      s.withProfile(PureDebugProfile.Name)
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
