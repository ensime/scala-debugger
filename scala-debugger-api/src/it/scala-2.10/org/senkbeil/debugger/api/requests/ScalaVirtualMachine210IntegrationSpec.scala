package org.senkbeil.debugger.api.requests

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

/** Specific to Scala 2.10 */
class ScalaVirtualMachine210IntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(2, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("ScalaVirtualMachine for 2.10") {
    it("should return the breakpointable line numbers for the file") {
      val testClass = "org.senkbeil.test.misc.AvailableLines"

      withVirtualMachine(testClass, suspend = false) { (_, scalaVirtualMachine) =>
        // NOTE: In Scala 2.10, there is a breakpoint available on the object
        //       itself (line 11), but there is not one on the last line of the
        //       object (72) - verified with IntelliJ
        val expected = Seq(
          11, 12, 13, 14, 15, 16, 20, 21, 22, 26, 27, 28, 32, 34, 35, 37, 39,
          40, 41, 42, 45, 46, 47, 50, 52, 53, 57, 58, 59, 60, 63, 65
        )

        val file = scalaClassStringToFileString(testClass)

        // There is some delay while receiving the Java classes that make up
        // our file, so must wait for enough responses to get all of our lines
        eventually {
          val actual = scalaVirtualMachine.availableLinesForFile(file).get
          actual should contain theSameElementsInOrderAs expected
        }
      }
    }
  }
}
