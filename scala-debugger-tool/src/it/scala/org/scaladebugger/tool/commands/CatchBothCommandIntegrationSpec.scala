package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.tool.frontend.VirtualTerminal
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, TestUtilities, ToolFixtures}

class CatchBothCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("CatchBothCommand") {
    it("should catch all exceptions (caught) if no filter provided") {

    }

    it("should catch all exceptions (uncaught) if no filter provided") {

    }

    it("should catch the specified exception (caught)") {
      val testClass = "org.scaladebugger.test.breakpoints.InsideTryBlockException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)
      val testExceptionName = "org.scaladebugger.test.exceptions.CustomException"

      // Create exception request before connecting to the JVM
      val q = "\""
      val virtualTerminal = new VirtualTerminal()
      virtualTerminal.newInputLine(s"catch $q$testExceptionName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our exception request was made
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val ers = svm.exceptionRequests
            ers should contain theSameElementsAs Seq(

            )
          }

          // Verify our breakpoints were set
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val brs = svm.breakpointRequests
              .map(bri => (bri.fileName, bri.lineNumber, bri.isPending))
            brs should contain theSameElementsAs Seq(
              (testFile, 10, false),
              (testFile, 11, false)
            )
          }

          // Assert that we hit the first breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:10\n")
          }

          // Continue on to the next breakpoint (resume main thread)
          vt.newInputLine("resume \"main\"")

          // Assert that we hit the second breakpoint
          eventually {
            validateNextLine(vt, s"Breakpoint hit at $testFileName:11\n")
          }
        })
      }
    }
  }
}
