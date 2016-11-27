package org.scaladebugger.api.profiles.scala210.info

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.scala210.Scala210DebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfoProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

class Scala210ObjectInfoProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("Scala210ObjectInfoProfile") {
    it("should expand $outer to its underlying fields") {
      val testClass = "org.scaladebugger.test.info.OuterScope"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfoProfile] = None
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
  }
}
