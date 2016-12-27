package org.scaladebugger.api.profiles.pure.info

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class PureTypeInfoIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("PureTypeInfo") {
    it("should be able to determine primitive and string types") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val vt = t.get.topFrame
            .allVariables.map(v => v.name -> v.typeInfo)
            .toMap

          // NOTE: Using assert to provide better feedback with failures
          // Check that each type is valid
          assert(vt("a").isBooleanType, s"a of ${vt("a").name} was not a boolean!")
          assert(vt("b").isCharType, s"b of ${vt("b").name} was not a character!")
          assert(vt("c").isShortType, s"c of ${vt("c").name} was not a short!")
          assert(vt("d").isIntegerType, s"d of ${vt("d").name} was not an integer!")
          assert(vt("e").isLongType, s"e of ${vt("e").name} was not a long!")
          assert(vt("f").isFloatType, s"e of ${vt("f").name} was not a float!")
          assert(vt("g").isDoubleType, s"e of ${vt("g").name} was not a double!")
          assert(vt("i").isArrayType, s"i of ${vt("i").name} was not an array!")
          assert(vt("z1").isByteType, s"z1 of ${vt("z1").name} was not a byte!")
          assert(vt("z2").isStringType, s"z2 of ${vt("z2").name} was not a string!")
        })
      }    }

    it("should be able to cast to primitive and string types") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val vt = t.get.topFrame
            .allVariables.map(v => v.name -> v.typeInfo)
            .toMap

          (vt("a").castLocal("true") == true) should be (true)
          vt("a").castLocal("true") shouldBe a [java.lang.Boolean]

          vt("b").castLocal("test") should be ('t')
          vt("b").castLocal("test") shouldBe a [java.lang.Character]

          vt("c").castLocal("33") should be (33)
          vt("c").castLocal("33.0") should be (33)
          vt("c").castLocal("33") shouldBe a [java.lang.Short]

          vt("d").castLocal("33") should be (33)
          vt("d").castLocal("33.0") should be (33)
          vt("d").castLocal("33") shouldBe an [java.lang.Integer]

          vt("e").castLocal("33") should be (33)
          vt("e").castLocal("33.0") should be (33)
          vt("e").castLocal("33") shouldBe a [java.lang.Long]

          vt("f").castLocal("33.5") should be (33.5)
          vt("f").castLocal("33.5") shouldBe a [java.lang.Float]

          vt("g").castLocal("33.5") should be (33.5)
          vt("g").castLocal("33.5") shouldBe a [java.lang.Double]

          vt("z1").castLocal("33") should be (33)
          vt("z1").castLocal("33.0") should be (33)
          vt("z1").castLocal("33") shouldBe a [java.lang.Byte]

          vt("z2").castLocal("33") should be ("33")
          vt("z2").castLocal("33") shouldBe a [String]
        })
      }
    }
  }
}
