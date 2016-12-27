package org.scaladebugger.api.virtualmachines

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ApiTestUtilities, VirtualMachineFixtures}

/** Specific to Scala 2.11 */
class ScalaVirtualMachine211IntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
  with Eventually
{
  describe("ScalaVirtualMachine for 2.11") {
    it("should return the breakpointable line numbers for the file") {
      val testClass = "org.scaladebugger.test.misc.AvailableLines"

      withVirtualMachine(testClass) { (s) =>
        // NOTE: In Scala 2.11, there is no breakpoint available on the object
        //       itself (line 11), but there is one on the last line of the
        //       object (72) - verified with IntelliJ
        val expected = Seq(
          12, 13, 14, 15, 16, 20, 21, 22, 26, 27, 28, 32, 34, 35, 37, 39,
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
