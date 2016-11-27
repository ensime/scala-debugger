package org.scaladebugger.api.profiles.pure.threads
import acyclic.file

import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureThreadDeathProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureThreadDeathProfile") {
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
