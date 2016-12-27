package org.scaladebugger.tool.commands

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class WatchBothCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("WatchBothCommand") {
    it("should watch the specified variable when accessed") {
      val testClass = "org.scaladebugger.test.watchpoints.AccessWatchpoint"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val className = "org.scaladebugger.test.watchpoints.SomeAccessClass"
      val fieldName = "field"

      // Create watch request before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"watch $q$className$q $q$fieldName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our watch request was made
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val awrs = svm.accessWatchpointRequests.map(awr =>
              (awr.className, awr.fieldName, awr.isPending))
            awrs should contain theSameElementsAs Seq(
              (className, fieldName, false)
            )
          }

          // Verify that we had a variable accessed
          eventually {
            val line = vt.nextOutputLine()

            // If we get a modified command, resume
            if (line.get.contains("modified")) {
              vt.newInputLine("resume")
            } else {
              line.get should startWith (s"'$fieldName' of '$className' accessed")
            }
          }

          // Main thread should be suspended
          eventually {
            val svm = sm.state.scalaVirtualMachines.head

            // NOTE: Using assert for better error message
            assert(svm.thread("main").status.isSuspended,
              "Main thread was not suspended!")
          }
        })
      }
    }

    it("should watch the specified variable when modified") {
      val testClass = "org.scaladebugger.test.watchpoints.ModificationWatchpoint"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val className = "org.scaladebugger.test.watchpoints.SomeModificationClass"
      val fieldName = "field"

      // Create watch request before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"watch $q$className$q $q$fieldName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our watch request was made
          eventually {
            val svm = sm.state.scalaVirtualMachines.head
            val mwrs = svm.modificationWatchpointRequests.map(mwr =>
              (mwr.className, mwr.fieldName, mwr.isPending))
            mwrs should contain theSameElementsAs Seq(
              (className, fieldName, false)
            )
          }

          // Verify that we had a variable accessed
          eventually {
            validateNextLine(
              vt, s"'$fieldName' of '$className' modified",
              success = (text, line) => line should startWith regex text
            )
          }

          // Main thread should be suspended
          eventually {
            val svm = sm.state.scalaVirtualMachines.head

            // NOTE: Using assert for better error message
            assert(svm.thread("main").status.isSuspended,
              "Main thread was not suspended!")
          }
        })
      }
    }
  }
}
