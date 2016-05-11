package org.scaladebugger.api.profiles.pure.vm
import acyclic.file

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureVMDeathProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureVMDeathProfile") {
    it("should trigger when a virtual machine dies") {
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
        val p  = s.underlyingVirtualMachine.process()
        p.destroy()
        p.waitFor()

        // Eventually, we should receive the start event
        logTimeTaken(eventually {
          detectedDeath.get() should be (true)
        })
      }
    }
  }
}
