package org.scaladebugger.api.virtualmachines

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span, Milliseconds}
import org.scalatest.{ParallelTestExecution, FunSpec, Matchers}
import test.{TestUtilities, VirtualMachineFixtures}

class StandardScalaVirtualMachineIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("StandardScalaVirtualMachine") {
    it("should indicate that it has started upon receiving the start event") {
      val testClass = "org.scaladebugger.test.misc.MainUsingMethod"

      withVirtualMachine(testClass) { (s) =>
        eventually {
          assert(s.isStarted, "ScalaVirtualMachine not started!")
        }
      }
    }
  }
}
