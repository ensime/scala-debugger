package org.scaladebugger.api.virtualmachines

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

/** Specific to Scala 2.12 */
class ScalaVirtualMachine212IntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  describe("ScalaVirtualMachine for 2.12") {
    it("should return the breakpointable line numbers for the file") {
      val testClass = "org.scaladebugger.test.misc.AvailableLines"

      withVirtualMachine(testClass) { (s) =>
        // NOTE: In Scala 2.12, there is a breakpoint available on the object
        //       itself (line 11), and there is one on the last line of the
        //       object (72)
        val expected = Seq(
          11, 12, 13, 14, 15, 16, 20, 21, 22, 26, 27, 28, 32, 34, 35, 37, 39,
          40, 41, 42, 45, 46, 47, 50, 52, 53, 57, 58, 59, 60, 63, 65, 72
        )

        val file = JDITools.scalaClassStringToFileString(testClass)

        // There is some delay while receiving the Java classes that make up
        // our file, so must wait for enough responses to get all of our lines
        eventually {
          val actual = s.availableLinesForFile(file).get
          actual should contain theSameElementsInOrderAs expected
        }
      }
    }
  }
}
