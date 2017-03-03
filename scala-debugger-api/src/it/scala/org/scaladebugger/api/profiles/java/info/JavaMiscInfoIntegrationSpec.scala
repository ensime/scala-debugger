package org.scaladebugger.api.profiles.java.info
import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class JavaMiscInfoIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("JavaMiscInfo") {
    it("should return the source paths for a given source name") {
      val testClass = "org.scaladebugger.test.info.MultiSource"

      withVirtualMachine(testClass) { (s) =>
        val expected = Seq(
          "org/scaladebugger/test/info/package1/ScalaSource.scala",
          "org/scaladebugger/test/info/package2/ScalaSource.scala"
        ).map(_.replace('/', java.io.File.separatorChar))
        val fileName = "ScalaSource.scala"

        eventually {
          val actual = s.withProfile(JavaDebugProfile.Name)
            .sourceNameToPaths(fileName)

          actual should contain theSameElementsAs (expected)
        }
      }
    }

    it("should be able to create values on the remote JVM") {
      val testClass = "org.scaladebugger.test.misc.MainUsingMethod"

      withVirtualMachine(testClass) { (s) =>
        eventually {
          val p = s.withProfile(JavaDebugProfile.Name)

          val remoteBoolean = p.createRemotely(true)
          remoteBoolean.toPrimitiveInfo.isBoolean should be (true)
          remoteBoolean.toLocalValue == true should be (true)

          val remoteByte = p.createRemotely(33.toByte)
          remoteByte.toPrimitiveInfo.isByte should be (true)
          remoteByte.toLocalValue == 33 should be (true)

          val remoteChar = p.createRemotely(33.toChar)
          remoteChar.toPrimitiveInfo.isChar should be (true)
          remoteChar.toLocalValue == 33 should be (true)

          val remoteInt = p.createRemotely(33.toInt)
          remoteInt.toPrimitiveInfo.isInteger should be (true)
          remoteInt.toLocalValue == 33 should be (true)

          val remoteShort = p.createRemotely(33.toShort)
          remoteShort.toPrimitiveInfo.isShort should be (true)
          remoteShort.toLocalValue == 33 should be (true)

          val remoteLong = p.createRemotely(33.toLong)
          remoteLong.toPrimitiveInfo.isLong should be (true)
          remoteLong.toLocalValue == 33 should be (true)

          val remoteFloat = p.createRemotely(33.toFloat)
          remoteFloat.toPrimitiveInfo.isFloat should be (true)
          remoteFloat.toLocalValue == 33 should be (true)

          val remoteDouble = p.createRemotely(33.toDouble)
          remoteDouble.toPrimitiveInfo.isDouble should be (true)
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
          val actual = s.withProfile(JavaDebugProfile.Name).mainClassName
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
          val actual = s.withProfile(JavaDebugProfile.Name).mainClassName
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
          val actual = s.withProfile(JavaDebugProfile.Name).commandLineArguments
          actual should contain theSameElementsInOrderAs expected
        }
      }
    }
  }
}
