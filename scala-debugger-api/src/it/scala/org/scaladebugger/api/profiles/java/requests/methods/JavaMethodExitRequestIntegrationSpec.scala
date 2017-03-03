package org.scaladebugger.api.profiles.java.requests.methods

import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class JavaMethodExitRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("JavaMethodExitRequest") {
    it("should be able to detect exiting a specific method in a class") {
      val testClass = "org.scaladebugger.test.methods.MethodExit"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val expectedClassName =
        "org.scaladebugger.test.methods.MethodExitTestClass"
      val expectedMethodName = "testMethod"

      val leftUnexpectedMethod = new AtomicBoolean(false)
      val leftExpectedMethod = new AtomicBoolean(false)
      val leftMethodAfterLastLine = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      val methodPipeline = s.withProfile(JavaDebugProfile.Name)
        .getOrCreateMethodExitRequest(expectedClassName, expectedMethodName)
        .map(_.method)
        .map(m => (m.declaringType.name, m.name))

      methodPipeline
        .filter(_._1 == expectedClassName)
        .filter(_._2 == expectedMethodName)
        .foreach(_ => leftExpectedMethod.set(true))

      methodPipeline
        .filterNot(_._1 == expectedClassName)
        .foreach(_ => leftUnexpectedMethod.set(true))

      methodPipeline
        .filterNot(_._2 == expectedMethodName)
        .foreach(_ => leftUnexpectedMethod.set(true))

      // Last line in test method
      s.getOrCreateBreakpointRequest(testFile, 28)
        .map(_.location)
        .map(l => (l.sourcePath, l.lineNumber))
        .foreach(t => {
        val methodExitHit = leftExpectedMethod.get()
        leftMethodAfterLastLine.set(!methodExitHit)
      })

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          leftUnexpectedMethod.get() should be (false)
          leftExpectedMethod.get() should be (true)
          leftMethodAfterLastLine.get() should be (true)
        })
      }
    }
  }
}
