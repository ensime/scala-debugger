package org.scaladebugger.api.profiles.java.info

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ApiTestUtilities, VirtualMachineFixtures}

class JavaFieldInfoScala210IntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
  with Eventually
{
  describe("JavaFieldInfo for 2.10") {
    it("should not expand $outer to its underlying fields") {
      val testClass = "org.scaladebugger.test.info.OuterScope"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(JavaDebugProfile.Name)
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
            "$outer",
            "newValue"
          )
        })
      }
    }

    it("should not fix Scala-specific field names like org$scaladebugger$test$bugs$BugFromGitter$$name") {
      val testClass = "org.scaladebugger.test.bugs.BugFromGitter"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 20, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val fieldNames = t.get.topFrame.allVariables.map(_.name)

          fieldNames should contain theSameElementsAs Seq(
            "actualTimes",
            "times",
            "org$scaladebugger$test$bugs$BugFromGitter$$name"
          )
        })
      }
    }

  }
}
