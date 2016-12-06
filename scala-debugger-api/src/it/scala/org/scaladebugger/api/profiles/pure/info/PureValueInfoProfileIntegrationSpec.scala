package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfoProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

import scala.collection.immutable.TreeMap

class PureValueInfoProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("PureValueInfoProfile") {
    it("should be able to get the type name for a variable's value") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfoProfile] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val variableNamesAndTypes = t.get
            .topFrame
            .allVariables.map(v => v.name -> v.typeInfo.name)
            .toMap

          variableNamesAndTypes should contain theSameElementsAs Map(
            // Scala-specific variable
            "MODULE$" -> "org.scaladebugger.test.info.Variables$",

            // Local argument variables
            "args" -> "java.lang.String[]",

            // Local non-argument variables
            "a" -> "boolean",
            "b" -> "char",
            "c" -> "short",
            "d" -> "int",
            "e" -> "long",
            "f" -> "float",
            "g" -> "double",
            "h" -> "scala.runtime.ObjectRef",
            "i" -> "int[]",
            "j" -> "scala.collection.immutable.List", // Old test was scala.collection.immutable.$colon$colon
            "k" -> "java.lang.Object[]",
            "l" -> "org.scaladebugger.test.info.Variables$NullToString$",

            // Field variables
            "z1" -> "byte",
            "z2" -> "java.lang.String",
            "z3" -> "scala.runtime.Null$" // Old test was "null"
          )
        })
      }
    }

    it("should be able to determine if a value is a primitive") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfoProfile] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val variableNamesAndTypes = t.get
            .topFrame
            .allVariables.map(v => v.name -> v.toValueInfo.isPrimitive)
            .toMap

          variableNamesAndTypes should contain theSameElementsAs Map(
            // Scala-specific variable
            "MODULE$" -> false,

            // Local argument variables
            "args" -> false,

            // Local non-argument variables
            "a" -> true,
            "b" -> true,
            "c" -> true,
            "d" -> true,
            "e" -> true,
            "f" -> true,
            "g" -> true,
            "h" -> false,
            "i" -> false,
            "j" -> false,
            "k" -> false,
            "l" -> false,

            // Field variables
            "z1" -> true,
            "z2" -> false,
            "z3" -> false
          )
        })
      }
    }

    it("should be able to determine if a value is an array") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfoProfile] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val variableNamesAndTypes = t.get
            .topFrame
            .allVariables.map(v => v.name -> v.toValueInfo.isArray)
            .toMap

          variableNamesAndTypes should contain theSameElementsAs Map(
            // Scala-specific variable
            "MODULE$" -> false,

            // Local argument variables
            "args" -> true,

            // Local non-argument variables
            "a" -> false,
            "b" -> false,
            "c" -> false,
            "d" -> false,
            "e" -> false,
            "f" -> false,
            "g" -> false,
            "h" -> false,
            "i" -> true,
            "j" -> false,
            "k" -> true,
            "l" -> false,

            // Field variables
            "z1" -> false,
            "z2" -> false,
            "z3" -> false
          )
        })
      }
    }

    it("should be able to determine if a value is an object") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfoProfile] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val variableNamesAndTypes = t.get
            .topFrame
            .allVariables.map(v => v.name -> v.toValueInfo.isObject)
            .toMap

          variableNamesAndTypes should contain theSameElementsAs Map(
            // Scala-specific variable
            "MODULE$" -> true,

            // Local argument variables
            "args" -> true,

            // Local non-argument variables
            "a" -> false,
            "b" -> false,
            "c" -> false,
            "d" -> false,
            "e" -> false,
            "f" -> false,
            "g" -> false,
            "h" -> true,
            "i" -> true,
            "j" -> true,
            "k" -> true,
            "l" -> true,

            // Field variables
            "z1" -> false,
            "z2" -> true,
            "z3" -> false
          )
        })
      }
    }

    it("should be able to determine if a value is a string") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfoProfile] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val variableNamesAndTypes = t.get
            .topFrame
            .allVariables.map(v => v.name -> v.toValueInfo.isString)
            .toMap

          variableNamesAndTypes should contain theSameElementsAs Map(
            // Scala-specific variable
            "MODULE$" -> false,

            // Local argument variables
            "args" -> false,

            // Local non-argument variables
            "a" -> false,
            "b" -> false,
            "c" -> false,
            "d" -> false,
            "e" -> false,
            "f" -> false,
            "g" -> false,
            "h" -> false, // Should be a string, but is an object reference
            "i" -> false,
            "j" -> false,
            "k" -> false,
            "l" -> false,

            // Field variables
            "z1" -> false,
            "z2" -> true,
            "z3" -> false
          )
        })
      }
    }
  }
}
