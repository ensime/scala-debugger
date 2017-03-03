package org.scaladebugger.api.profiles.java.requests.classes

import java.util.concurrent.atomic.AtomicInteger

import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class JavaClassPrepareRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("JavaClassPrepareRequest") {
    it("should trigger when a class is loaded") {
      val testClass = "org.scaladebugger.test.classes.ClassPrepare"

      val expectedClassName = "org.scaladebugger.test.classes.CustomClass"
      val classPrepareHit = new AtomicInteger(0)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive class prepare events and watch for one
      // NOTE: This is already set within the ScalaVirtualMachine class
      s.withProfile(JavaDebugProfile.Name)
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
