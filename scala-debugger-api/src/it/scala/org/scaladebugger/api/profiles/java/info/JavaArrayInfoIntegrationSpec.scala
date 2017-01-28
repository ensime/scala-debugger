package org.scaladebugger.api.profiles.java.info

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class JavaArrayInfoIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("JavaArrayInfo") {
    it("should be able to return the length of the array") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val array = t.get.topFrame.variable("i").toValueInfo.toArrayInfo

          array.length should be (3)
        })
      }
    }

    it("should be able to return an element at a specific position") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val array = t.get.topFrame.variable("i").toValueInfo.toArrayInfo

          array(1).toLocalValue should be (2)
        })
      }
    }

    it("should be able to return a range of elements") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val array = t.get.topFrame.variable("i").toValueInfo.toArrayInfo

          array.values(1, 2).map(_.toLocalValue) should be (Seq(2, 3))
        })
      }
    }

    it("should be able to return all elements") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val array = t.get.topFrame.variable("i").toValueInfo.toArrayInfo

          array.values.map(_.toLocalValue) should be (Seq(1, 2, 3))
        })
      }
    }

    it("should be able to set a value at a specific position") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val array = t.get.topFrame.variable("i").toValueInfo.toArrayInfo

          array(1) = 999
          array(1).toLocalValue should be (999)
        })
      }
    }

    it("should be able to set a range of values") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val array = t.get.topFrame.variable("i").toValueInfo.toArrayInfo

          // Set element at position 1 to source element 2 (12)
          val results = array.setValues(1, Seq(10, 11, 12), 2, 1).map(_.toLocalValue)
          results should be (Seq(12))
        })
      }
    }

    it("should be able to set all values") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadInfo] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(JavaDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val array = t.get.topFrame.variable("i").toValueInfo.toArrayInfo

          val results = array.setValues(Seq(10, 11, 12)).map(_.toLocalValue)
          results should be (Seq(10, 11, 12))
        })
      }
    }
  }
}
