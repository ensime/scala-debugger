package org.scaladebugger.api.profiles.pure.requests.classes

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureClassPrepareProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureClassPrepareProfile") {
    it("should trigger when a class is loaded") {
      val testClass = "org.scaladebugger.test.classes.ClassPrepare"

      val expectedClassName = "org.scaladebugger.test.classes.CustomClass"
      val classPrepareHit = new AtomicInteger(0)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive class prepare events and watch for one
      // NOTE: This is already set within the ScalaVirtualMachine class
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateClassPrepareRequest()
        .map(_.referenceType.name)
        .filter(_ == expectedClassName)
        .foreach(_ => classPrepareHit.incrementAndGet())

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Eventually, we should receive the class prepare event
        logTimeTaken(eventually {
          classPrepareHit.get() should be (1)
        })
      }
    }
  }
}
