package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

class PureReferenceTypeInfoIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("PureReferenceTypeInfo") {
    it("should look up the source path using the Java package name") {
      val testClass = "org.scaladebugger.test.invalid.InvalidSourcePath"
      val testFile = "org/scaladebugger/test/misc/InvalidSourcePath.scala"

      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the class
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 14, NoResume)

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val c = s.withProfile(PureDebugProfile.Name)
            .`class`("org.scaladebugger.test.invalid.InvalidSourcePathClass")

          // NOTE: Would love for this to be the Scala source path, but that
          //       gets lost when compiling to Java as the package name is
          //       used to generate the Java directory for the output source
          c.sourcePaths.head should not be (testFile)
          c.sourceNames.head should be ("InvalidSourcePath.scala")
        })
      }
    }

    it("should be able to retrieve a specific field from the class") {
      val testClass = "org.scaladebugger.test.info.Classes"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withVirtualMachine(testClass) { (s) =>
        logTimeTaken(eventually {
          val ccc = s.withProfile(PureDebugProfile.Name).classes
          val fieldName = s.withProfile(PureDebugProfile.Name)
            .classes
            .find(_.name == "org.scaladebugger.test.info.ExternalCaseClass")
            .get
            .field("x").name

          fieldName should be ("x")
        })
      }
    }

    it("should be able to get a list of fields for the class") {
      val testClass = "org.scaladebugger.test.info.Classes"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withVirtualMachine(testClass) { (s) =>
        logTimeTaken(eventually {
          val fieldNames = s.withProfile(PureDebugProfile.Name)
            .classes
            .find(_.name == "org.scaladebugger.test.info.ExternalCaseClass")
            .get
            .allFields
            .map(_.name)

          fieldNames should contain theSameElementsAs Seq(
            "x", "y"
          )
        })
      }
    }

    it("should be able to retrieve specific methods by name from the class") {
      val testClass = "org.scaladebugger.test.info.Classes"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withVirtualMachine(testClass) { (s) =>
        logTimeTaken(eventually {
          val methodNames = s.withProfile(PureDebugProfile.Name)
            .classes
            .find(_.name == "org.scaladebugger.test.info.ExternalNormalClass")
            .get
            .methods("method1")
            .map(_.name)

          methodNames should be (Seq("method1"))
        })
      }
    }

    it("should be able to get a list of methods for the class") {
      val testClass = "org.scaladebugger.test.info.Classes"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withVirtualMachine(testClass) { (s) =>
        logTimeTaken(eventually {
          val methodNames = s.withProfile(PureDebugProfile.Name)
            .classes
            .find(_.name == "org.scaladebugger.test.info.ExternalNormalClass")
            .get
            .allMethods
            .map(_.name)

          methodNames should contain theSameElementsAs Seq(
            // Defined methods
            "method1",
            "method2",
            "method3",

            // Inherited methods
            "<clinit>",
            "<init>", // Overloaded method
            "<init>",
            "registerNatives",
            "getClass",
            "hashCode",
            "equals",
            "clone",
            "toString",
            "notify",
            "notifyAll",
            "wait", // Overloaded method
            "wait",
            "wait",
            "finalize"
          )
        })
      }
    }
  }
}
