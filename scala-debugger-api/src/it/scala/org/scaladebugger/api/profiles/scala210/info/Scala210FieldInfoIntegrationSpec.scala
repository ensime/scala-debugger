package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.scala210.Scala210DebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

class Scala210FieldInfoIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("Scala210FieldInfo") {
    it("should fix Scala-specific field names like org$scaladebugger$test$bugs$BugFromGitter$$name") {
      val testClass = "org.scaladebugger.test.bugs.BugFromGitter"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(Scala210DebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 20, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val fieldNames = t.get.topFrame.allVariables.map(_.name)

          fieldNames should contain theSameElementsAs Seq(
            "actualTimes",
            "times",
            "name" // Indicates fixed
          )

          // Can also retrieve field with fixed name
          val fieldValue = t.get
            .findVariableByName("name")
            .get
            .toValueInfo
            .toLocalValue

          fieldValue should be ("Rory")
        })
      }
    }

    // NOTE: field1 is a method that is invoked instead of a field that is
    //       accessed ~ need to figure out what methods are referenced inside
    //       a class, translate relevant ones to fields, and display
    //       appropriately
    //
    //       Think I can use the MODULE$ as a flag to import any fields
    //       (or field methods) hanging off of it
    //
    //       If not, parse to understand that it is an inner class and
    //       get fields of parent class, since they will be in scope
    //
    //       See http://www.artima.com/pins1ed/combining-scala-and-java.html
    //
    ignore("should display fields brought into class closures") {
      val testClass = "org.scaladebugger.test.info.Scope"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(Scala210DebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 22, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val variableNames = t.get.topFrame.allVariables.map(_.name)

          variableNames should contain theSameElementsAs Seq(
            "field1",
            "a",
            "aa"
          )
        })
      }
    }

    // NOTE: For Scala 2.10/2.11, x/y are considered fields
    //       For Scala 2.12, x/y are considered arguments (local variables)
    //       So, for Scala 2.12, this is testing local variable implementation
    it("should translate names like x$1 to x for function closures") {
      val testClass = "org.scaladebugger.test.info.Scope"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(Scala210DebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 33, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val variableNames = t.get.topFrame.allVariables.map(_.name)

          variableNames should contain theSameElementsAs Seq(
            "x",
            "y"
          )
        })
      }
    }

    it("should translate names like x$1 to x for class closures") {
      val testClass = "org.scaladebugger.test.info.Scope"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(Scala210DebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 39, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val variableNames = t.get.topFrame.allVariables.map(_.name)

          variableNames should contain theSameElementsAs Seq(
            "x",
            "y",
            "z",
            "zz"
          )
        })
      }
    }
  }
}
