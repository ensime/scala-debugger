package org.scaladebugger.api.profiles.pure.info
import acyclic.file
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.utils.JDITools
import test.{TestUtilities, VirtualMachineFixtures}

class PureMiscInfoProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureMiscInfoProfile") {
    it("should return the source paths for a given source name") {
      val testClass = "org.scaladebugger.test.info.MultiSource"

      withVirtualMachine(testClass) { (s) =>
        val expected = Seq(
          "org/scaladebugger/test/info/package1/ScalaSource.scala",
          "org/scaladebugger/test/info/package2/ScalaSource.scala"
        )
        val fileName = "ScalaSource.scala"

        eventually {
          val actual = s.withProfile(PureDebugProfile.Name)
            .sourceNameToPaths(fileName)

          actual should contain theSameElementsAs (expected)
        }
      }
    }

    it("should be able to create values on the remote JVM") {
      val testClass = "org.scaladebugger.test.misc.MainUsingMethod"

      withVirtualMachine(testClass) { (s) =>
        eventually {
          val p = s.withProfile(PureDebugProfile.Name)

          val remoteBoolean = p.createRemotely(true)
          remoteBoolean.toPrimitive.isBoolean should be (true)
          remoteBoolean.toLocalValue == true should be (true)

          val remoteByte = p.createRemotely(33.toByte)
          remoteByte.toPrimitive.isByte should be (true)
          remoteByte.toLocalValue == 33 should be (true)

          val remoteChar = p.createRemotely(33.toChar)
          remoteChar.toPrimitive.isChar should be (true)
          remoteChar.toLocalValue == 33 should be (true)

          val remoteInt = p.createRemotely(33.toInt)
          remoteInt.toPrimitive.isInteger should be (true)
          remoteInt.toLocalValue == 33 should be (true)

          val remoteShort = p.createRemotely(33.toShort)
          remoteShort.toPrimitive.isShort should be (true)
          remoteShort.toLocalValue == 33 should be (true)

          val remoteLong = p.createRemotely(33.toLong)
          remoteLong.toPrimitive.isLong should be (true)
          remoteLong.toLocalValue == 33 should be (true)

          val remoteFloat = p.createRemotely(33.toFloat)
          remoteFloat.toPrimitive.isFloat should be (true)
          remoteFloat.toLocalValue == 33 should be (true)

          val remoteDouble = p.createRemotely(33.toDouble)
          remoteDouble.toPrimitive.isDouble should be (true)
          remoteDouble.toLocalValue == 33 should be (true)

          val remoteString = p.createRemotely("test")
          remoteString.isString should be (true)
          remoteString.toLocalValue  should be ("test")
        }
      }
    }

    it("should return the class name of a Scala main method entrypoint") {
      val testClass = "org.scaladebugger.test.misc.MainUsingMethod"

      withVirtualMachine(testClass) { (s) =>
        val expected = testClass

        // NOTE: This is not available until AFTER we have resumed from the
        //       start event (as the main method is not yet loaded)
        eventually {
          val actual = s.withProfile(PureDebugProfile.Name).mainClassName
          actual should be(expected)
        }
      }
    }

    it("should return the class name of a Scala App entrypoint") {
      val testClass = "org.scaladebugger.test.misc.MainUsingApp"

      withVirtualMachine(testClass) { (s) =>
        val expected = testClass

        // NOTE: This is not available until AFTER we have resumed from the
        //       start event (as the main method is not yet loaded)
        eventually {
          val actual = s.withProfile(PureDebugProfile.Name).mainClassName
          actual should be(expected)
        }
      }
    }

    it("should return the arguments provided to the virtual machine") {
      val testClass = "org.scaladebugger.test.misc.MainUsingApp"
      val testArguments = Seq("a", "b", "c")

      withVirtualMachine(testClass, testArguments) { (s) =>
        val expected = testArguments

        // NOTE: This is not available until AFTER we have resumed from the
        //       start event (as the main method is not yet loaded)
        eventually {
          val actual = s.withProfile(PureDebugProfile.Name).commandLineArguments
          actual should contain theSameElementsInOrderAs expected
        }
      }
    }
  }
}
