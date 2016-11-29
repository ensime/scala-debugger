package org.scaladebugger.api.profiles.pure.requests.monitors
import acyclic.file

import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureMonitorWaitedProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureMonitorWaitedProfile") {
    it("should trigger when a thread has finished waiting on a monitor object") {
      val testClass = "org.scaladebugger.test.monitors.MonitorWaited"

      val detectedWaited = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive monitor waited events and
      // watch for one
      s.withProfile(PureDebugProfile.Name)
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
