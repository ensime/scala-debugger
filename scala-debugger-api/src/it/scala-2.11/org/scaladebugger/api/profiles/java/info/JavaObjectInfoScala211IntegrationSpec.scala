package org.scaladebugger.api.profiles.java.info

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ApiTestUtilities, VirtualMachineFixtures}

class JavaObjectInfoScala211IntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
  with Eventually
{
  describe("JavaObjectInfo for 2.11") {
    it("should be able to get a list of methods for the object") {
      val testClass = "org.scaladebugger.test.info.Methods"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 22, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val methodNames = t.get.topFrame.thisObject.methods.map(_.name)

          methodNames should contain theSameElementsAs Seq(
            // Defined methods
            "main",
            "innerMethod$1", // Nested method has different Java signature
            "publicMethod",
            "privateMethod",
            "protectedMethod",
            "zeroArgMethod",
            "functionMethod", // Scala provides a method for the function
                              // object since it would be treated as a field

            // Inherited methods
            "<clinit>",
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
