package org.scaladebugger.api.profiles.pure.info

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class PureArrayTypeInfoIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("PureArrayTypeInfo") {
    it("should be able to retrieve information about the array's element type") {
      val testClass = "org.scaladebugger.test.info.ArrayType"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 15, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val array = t.get.topFrame.variable("array").toValueInfo.toArrayInfo

          val arrayType = array.typeInfo

          arrayType.elementTypeName should be ("int")
          arrayType.elementSignature should be ("I")
          arrayType.elementTypeInfo.name should be ("int")
        })
      }
    }

    it("should be able to create a new array instance from the type") {
      val testClass = "org.scaladebugger.test.info.ArrayType"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 13, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        val length = 80

        logTimeTaken(eventually {
          val array = t.get.topFrame.variable("array").toValueInfo.toArrayInfo
          val arrayType = array.typeInfo

          // TODO: Assign the new array to the tmpArray variable
          // Create a new array
          arrayType.newInstance(length).length should be (length)
        })
      }
    }
  }
}
