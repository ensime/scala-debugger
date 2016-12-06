package org.scaladebugger.api.profiles.pure.info

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfoProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

class PureInterfaceTypeInfoProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities
{
  describe("PureInterfaceTypeInfoProfile") {
    it("should be able to retrieve information about the interface type") {
      val testClass = "org.scaladebugger.test.info.ClassType"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfoProfile] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 68, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val frame = t.get.topFrame

          val classes = s.classes
          val baseInterfaceType = classes.find(_.name == "org.scaladebugger.test.info.ClassType$BaseInterface").get.toInterfaceType
          val interfaceFromBaseInterfaceType = classes.find(_.name == "org.scaladebugger.test.info.ClassType$InterfaceFromBaseInterface").get.toInterfaceType

          // Should be able to find implementors
          baseInterfaceType.implementors.map(_.name) should
            contain ("org.scaladebugger.test.info.ClassType$ClassFromBaseInterface")

          // Should be able to find sub interfaces
          baseInterfaceType.subinterfaces.map(_.name) should
            contain ("org.scaladebugger.test.info.ClassType$InterfaceFromBaseInterface")

          // Should be able to find sub interfaces
          interfaceFromBaseInterfaceType.superinterfaces.map(_.name) should
            contain ("org.scaladebugger.test.info.ClassType$BaseInterface")
        })
      }
    }
  }
}
