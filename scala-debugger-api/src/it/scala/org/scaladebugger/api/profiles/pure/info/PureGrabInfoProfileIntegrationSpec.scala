package org.scaladebugger.api.profiles.pure.info
import acyclic.file

import com.sun.jdi.{ThreadReference, Value}
import com.sun.jdi.event.BreakpointEvent
import org.scaladebugger.api.lowlevel.events.EventType
import org.scaladebugger.api.lowlevel.events.EventType.BreakpointEventType
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

import scala.util.Try

class PureGrabInfoProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureGrabInfoProfile") {
    it("should be able to list all active threads") {
      val testClass = "org.scaladebugger.test.misc.LaunchingMain"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withVirtualMachine(testClass) { (s) =>
        logTimeTaken(eventually {
          val threadNames = s.withProfile(PureDebugProfile.Name)
            .threads.map(_.name)

          threadNames should contain theSameElementsAs Seq(
            "Signal Dispatcher",
            "Finalizer",
            "Reference Handler",
            "main"
          )
        })
      }
    }

    it("should be able to find a thread by its unique id") {
      val testClass = "org.scaladebugger.test.misc.LaunchingMain"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadReference] = None
      val s = DummyScalaVirtualMachine.newInstance()
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 7).foreach(e => t = Some(e.thread()))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val id = t.get.uniqueID()

          s.withProfile(PureDebugProfile.Name)
            .thread(id).uniqueId should be (id)

          s.withProfile(PureDebugProfile.Name)
            .thread(t.get).uniqueId should be (id)
        })
      }
    }

    it("should be able to list top-level thread groups") {
      val testClass = "org.scaladebugger.test.misc.LaunchingMain"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withVirtualMachine(testClass) { (s) =>
        logTimeTaken(eventually {
          s.withProfile(PureDebugProfile.Name).threadGroups.map(_.name) should
            contain theSameElementsAs Seq("system")
        })
      }
    }

    it("should be able to find a thread group by its unique id") {
      val testClass = "org.scaladebugger.test.misc.LaunchingMain"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadReference] = None
      val s = DummyScalaVirtualMachine.newInstance()
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 7).foreach(e => t = Some(e.thread()))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val tg = t.get.threadGroup()
          val id = tg.uniqueID()

          s.withProfile(PureDebugProfile.Name)
            .threadGroup(id).uniqueId should be (id)

          s.withProfile(PureDebugProfile.Name)
            .threadGroup(tg).uniqueId should be (id)
        })
      }
    }
  }
}
