package org.scaladebugger.api.profiles.pure.info

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ApiTestUtilities, VirtualMachineFixtures}

class PureFieldInfoScala212IntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
  with Eventually
{
  describe("PureFieldInfo for 2.12") {
    // $outer does not appear in this scenario for Scala 2.12
    it("should not expand $outer to its underlying fields (no $outer in 2.12)") {
      val testClass = "org.scaladebugger.test.info.OuterScope"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
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
            "MODULE$",
            "x",
            "executionStart",
            "scala$App$$_args", // Scala 2.12 (_Args is now _args)
            "scala$App$$initCode",
            "newValue"
          )
        })
      }
    }

    it("should not encounter Scala-specific field names like org$scaladebugger$test$bugs$BugFromGitter$$name") {
      val testClass = "org.scaladebugger.test.bugs.BugFromGitter"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 20, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val fieldNames = t.get.topFrame.allVariables.map(_.name)

          fieldNames should contain theSameElementsAs Seq(
            "actualTimes",
            "times",
            "name"
          )
        })
      }
    }
  }
}
