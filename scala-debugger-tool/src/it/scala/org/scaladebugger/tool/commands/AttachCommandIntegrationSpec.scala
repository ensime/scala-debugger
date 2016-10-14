package org.scaladebugger.tool.commands

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{ToolFixtures, TestUtilities}

class AttachCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("AttachCommand") {
    it("should attach successfully attach using a port") {
      val testClass = "org.scaladebugger.test.breakpoints.WhileLoop"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withProcess(testClass) { (port) =>
        logTimeTaken(eventually {

        })
      }
    }
  }
}
