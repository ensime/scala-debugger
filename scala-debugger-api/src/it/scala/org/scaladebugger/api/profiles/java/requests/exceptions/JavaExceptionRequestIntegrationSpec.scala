package org.scaladebugger.api.profiles.java.requests.exceptions

import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class JavaExceptionRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("JavaExceptionRequest") {
    it("should be able to detect exceptions in try blocks") {
      val testClass =
        "org.scaladebugger.test.exceptions.InsideTryBlockException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.scaladebugger.test.exceptions.CustomException"

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark the exception we want to watch (now that the class
      // is available)
      s.withProfile(JavaDebugProfile.Name).getOrCreateExceptionRequest(
        exceptionName = expectedExceptionName,
        notifyCaught = true,
        notifyUncaught = false
      ).map(_.exception.referenceType.name)
        .filter(_ == expectedExceptionName)
        .foreach(_ => detectedException.set(true))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }

    it("should be able to detect exceptions in functional try calls") {
      val testClass =
        "org.scaladebugger.test.exceptions.InsideFunctionalTryException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.scaladebugger.test.exceptions.CustomException"

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark the exception we want to watch (now that the class
      // is available)
      s.withProfile(JavaDebugProfile.Name).getOrCreateExceptionRequest(
        exceptionName = expectedExceptionName,
        notifyCaught = true,
        notifyUncaught = false
      ).map(_.exception.referenceType.name)
        .filter(_ == expectedExceptionName)
        .foreach(_ => detectedException.set(true))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }

    it("should be able to detect exceptions outside of try blocks") {
      val testClass =
        "org.scaladebugger.test.exceptions.OutsideTryException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.scaladebugger.test.exceptions.CustomException"

      val s = DummyScalaVirtualMachine.newInstance()

      // Mark the exception we want to watch (now that the class
      // is available)
      s.withProfile(JavaDebugProfile.Name).getOrCreateExceptionRequest(
        exceptionName = expectedExceptionName,
        notifyCaught = false,
        notifyUncaught = true
      ).map(_.exception.referenceType.name)
        .filter(_ == expectedExceptionName)
        .foreach(_ => detectedException.set(true))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }
  }
}
