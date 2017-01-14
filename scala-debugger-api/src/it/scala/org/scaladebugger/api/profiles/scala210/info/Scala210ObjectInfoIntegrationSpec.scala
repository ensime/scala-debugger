package org.scaladebugger.api.profiles.scala210.info

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.scala210.Scala210DebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class Scala210ObjectInfoIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("Scala210ObjectInfo") {
    it("should expand $outer to its underlying fields") {
      val testClass = "org.scaladebugger.test.info.OuterScope"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(Scala210DebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 17, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val variableNames = t
            .flatMap(_.tryTopFrame.toOption)
            .map(_.allVariables)
            .map(_.map(_.name))
            .get

          // Should expand $outer field of closure into outer fields
          variableNames should contain theSameElementsAs Seq(
            "x", // Expanded from $outer
            "newValue"
          )
        })
      }
    }

    it("should be consistent in .fields() and .indexedFields() calls and with Scala210ReferenceType") {
      val testClass = "org.scaladebugger.test.info.Fields"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(Scala210DebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 63, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val inheritedClassVar = t
            .flatMap(_.findVariableByName("inheritedClass"))
          val inheritedClassObject = inheritedClassVar
            .map(_.toValueInfo.toObjectInfo)
          val inheritedClassTypeRef = inheritedClassVar
            .map(_.toValueInfo.`type`.toReferenceType)

          val fieldNames = inheritedClassObject
            .toList
            .flatMap(_.fields)
            .map(_.toJdiInstance.name)
          val indexedFieldNames = inheritedClassObject
            .toList
            .flatMap(_.indexedFields)
            .map(_.toJdiInstance.name)
          val typeRefVisibleFieldNames = inheritedClassTypeRef
            .toList
            .flatMap(_.visibleFields)
            .map(_.toJdiInstance.name)

          fieldNames.length should ===(4)
          indexedFieldNames.length should ===(4)
          typeRefVisibleFieldNames.length should ===(4)

          fieldNames should (
            contain theSameElementsAs indexedFieldNames and
            contain theSameElementsAs typeRefVisibleFieldNames
          )

        })
      }
    }
  }
}
