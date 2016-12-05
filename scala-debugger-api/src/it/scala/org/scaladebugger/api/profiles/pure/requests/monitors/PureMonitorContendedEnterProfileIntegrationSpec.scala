package org.scaladebugger.api.profiles.pure.requests.monitors

import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureMonitorContendedEnterProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureMonitorContendedEnterProfile") {
    it("should trigger when a thread attempts to enter a monitor already acquired by another thread") {
      val testClass = "org.scaladebugger.test.monitors.MonitorContendedEnter"

      val detectedEnter = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive monitor contended enter events and
      // watch for one
      s.withProfile(PureDebugProfile.Name)
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
