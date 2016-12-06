package org.scaladebugger.api.profiles.pure.requests.vm
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution, Tag}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{Constants, TestUtilities, VirtualMachineFixtures}

class PureVMDeathProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("PureVMDeathProfile") {
    it("should trigger when a virtual machine dies", Constants.NoWindows) {
      val testClass = "org.scaladebugger.test.misc.MainUsingApp"

      val detectedDeath = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive vm death events and watch for one
      s.withProfile(PureDebugProfile.Name)
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
