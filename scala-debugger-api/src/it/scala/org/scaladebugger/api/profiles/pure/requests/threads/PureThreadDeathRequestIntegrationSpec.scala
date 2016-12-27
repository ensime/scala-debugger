package org.scaladebugger.api.profiles.pure.requests.threads

import java.util.concurrent.atomic.AtomicInteger

import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class PureThreadDeathRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("PureThreadDeathRequest") {
    it("should trigger when a thread dies") {
      val testClass = "org.scaladebugger.test.threads.ThreadDeath"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val threadDeathCount = new AtomicInteger(0)

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark that we want to receive thread death events
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateThreadDeathRequest()
        .map(_.thread.name)
        .filter(_.startsWith("test thread"))
        .foreach(_ => threadDeathCount.incrementAndGet())

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        // Eventually, we should receive a total of 10 thread deaths
        logTimeTaken(eventually {
          threadDeathCount.get() should be (10)
        })
      }
    }
  }
}
