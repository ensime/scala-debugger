package org.scaladebugger.api.profiles.java.requests.steps
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.time.Seconds
import test.{ApiTestUtilities, VirtualMachineFixtures}

class JavaStepRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("JavaStepRequest") {
    describe("stepping out of") {
      it("should be able to finish executing a method and return to the next line in the parent frame") {
        val testClass = "org.scaladebugger.test.steps.MethodCalls"

        // Start on first line of a method
        val startingLine = 15

        // Should return to higher frame on next line
        val expectedLine = 35

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsFromTo(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedLine = expectedLine
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepOutLine(_: ThreadInfo))
        }
      }

      it("should be able to finish executing a function and return to the next line in the parent frame") {
        val testClass = "org.scaladebugger.test.steps.FunctionCalls"

        // Start on first line of a method
        val startingLine = 18

        // Should return to higher frame on next line
        val expectedLine = 41

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsFromTo(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedLine = expectedLine
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepOutLine(_: ThreadInfo))
        }
      }
    }

    describe("stepping over") {
      it("should be able to step over declarations and assignments") {
        val testClass = "org.scaladebugger.test.steps.BasicAssignments"

        // Start on first line of main method
        val startingLine = 13

        val expectedReachableLines = Seq(14, 16, 18)

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsOnEach(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedReachableLines = expectedReachableLines
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepOverLine(_: ThreadInfo))
        }
      }

      it("should be able to step back out to higher frame once method finishes") {
        val testClass = "org.scaladebugger.test.steps.MethodCalls"

        // Start on last line of a method
        val startingLine = 28

        // Should return to higher frame on next line
        val expectedLine = 39

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsFromTo(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedLine = expectedLine
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepOverLine(_: ThreadInfo))
        }
      }

      it("should be able to step over all lines in a method") {
        val testClass = "org.scaladebugger.test.steps.MethodCalls"

        // Start on first line of main method
        val startingLine = 31

        val expectedReachableLines = Seq(33, 34, 35, 37, 38, 39, 41, 42, 44)

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsOnEach(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedReachableLines = expectedReachableLines
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepOverLine(_: ThreadInfo))
        }
      }
    }

    describe("stepping into") {
      // TODO: This cannot be done (gets stuck in strings and classloaders)
      //       until we add filtering of Boxed types and Classloaders
      ignore("should enter all iterations except for comprehension") {
        val testClass = "org.scaladebugger.test.steps.BasicIterations"

        // Start on first line of main method
        val startingLine = 13

        // Running through multiple scenarios in this test
        // NOTE: These expectations were made based off of IntelliJ's handling
        val expectedReachableLines = Seq(
          /*
           * If prefixed with s, referencing org.scaladebugger.test.helpers.Stub…
           *  16, s10, 16,
           *  21, 22, 21, 22, s10, 22, s10, 22, s10, 22, 21,
           *  26, 27, 26, 27, s11, 27, s11, 27, s11, 27, 26,
           *  31, 31, s11, 31, s11, 31, s11, 31, 31,
           *  36,
           *  45, 37, s10, 37, s10, 37, s10, 37,
           *  47, 41, 42, 41, 42, s10, 42, s10, 42, s10, 42, 41,
           *  49, s10, 49
           */
        )

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsOnEach(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedReachableLines = expectedReachableLines,
          failIfNotExact = true,
          maxDuration = (15, Seconds)
        )

        // NOTE: Have to up the maximum duration due to the delay caused by
        //       the for comprehension
        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepIntoLine(_: ThreadInfo))
        }
      }

      it("should be able to step into a function in a class") {
        val testClass = "org.scaladebugger.test.steps.FunctionCalls"

        val startingLine = 48

        // Should first go to the function definition, then back to the
        // invoking line, and finally to the inside of the function
        val expectedReachableLines = Seq(56, 48, 57)

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsOnEach(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedReachableLines = expectedReachableLines
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepIntoLine(_: ThreadInfo))
        }
      }

      it("should be able to step into a method in a class") {
        val testClass = "org.scaladebugger.test.steps.MethodCalls"

        val startingLine = 42
        val expectedLine = 49

        val s = DummyScalaVirtualMachine.newInstance()
        val verify = verifyStepsFromTo(
          testClass = testClass,
          scalaVirtualMachine = s,
          startingLine = startingLine,
          expectedLine = expectedLine
        )

        withLazyVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s, f) =>
          verify(s, f, s.stepIntoLine(_: ThreadInfo))
        }
      }
    }
  }
}
