package org.scaladebugger.api.profiles.pure.requests.vm

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureVMDisconnectProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("PureVMDisconnectProfile") {
    it("should trigger when a virtual machine disconnects") {
      val testClass = "org.scaladebugger.test.misc.MainUsingApp"

      val detectedDisconnect = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      s.withProfile(PureDebugProfile.Name)
        .getOrCreateVMDisconnectRequest()
        .foreach(_ => detectedDisconnect.set(true))

      // Start our VM and listen for the disconnect event
      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Kill the JVM process so we get a disconnect event
        s.underlyingVirtualMachine.process().destroy()

        // Eventually, we should receive the disconnect event
        logTimeTaken(eventually {
          detectedDisconnect.get() should be (true)
        })
      }
    }
  }
}
