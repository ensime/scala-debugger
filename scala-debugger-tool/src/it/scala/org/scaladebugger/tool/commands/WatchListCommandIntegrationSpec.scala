package org.scaladebugger.tool.commands

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{Constants, FixedParallelSuite, TestUtilities, ToolFixtures}

class WatchListCommandIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with ToolFixtures
  with TestUtilities with Eventually with FixedParallelSuite
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Constants.EventuallyTimeout),
    interval = scaled(Constants.EventuallyInterval)
  )

  describe("WatchListCommand") {
    it("should list pending and active watchpoint requests") {
      val testClass = "org.scaladebugger.test.watchpoints.AccessWatchpoint"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val className = "org.scaladebugger.test.watchpoints.SomeAccessClass"
      val fieldName = "field"

      val fakeClassName = "invalid.class"
      val fakeFieldName = "fakeField"

      // Create watch request before connecting to the JVM
      val q = "\""
      val virtualTerminal = newVirtualTerminal()
      virtualTerminal.newInputLine(s"watch $q$className$q $q$fieldName$q")
      virtualTerminal.newInputLine(s"watch $q$fakeClassName$q $q$fakeFieldName$q")

      withToolRunningUsingTerminal(
        className = testClass,
        virtualTerminal = virtualTerminal
      ) { (vt, sm, start) =>
        logTimeTaken({
          // Verify our watch requests were made
          eventually {
            val svm = sm.state.scalaVirtualMachines.head

            val awrs = svm.accessWatchpointRequests.map(awr =>
              (awr.className, awr.fieldName, awr.isPending))
            awrs should contain theSameElementsAs Seq(
              (className, fieldName, false),
              (fakeClassName, fakeFieldName, true)
            )

            val mwrs = svm.modificationWatchpointRequests.map(awr =>
              (awr.className, awr.fieldName, awr.isPending))
            mwrs should contain theSameElementsAs Seq(
              (className, fieldName, false),
              (fakeClassName, fakeFieldName, true)
            )
          }

          // List all available watchpoints
          vt.newInputLine("watchlist")

          // First prints out JVM id
          eventually {
            validateNextLine(vt, "JVM",
              success = (text, line) => line should include(text))
          }

          // Verify expected pending and active requests show up
          // by collecting the three available and checking their content
          val lines = Seq(nextLine(vt), nextLine(vt), nextLine(vt), nextLine(vt)).flatten
          lines should contain allOf(
            s"{Class $className}\n",
            s"-> Field '$fieldName' [Access: Active] [Modification: Active]\n",
            s"{Class $fakeClassName}\n",
            s"-> Field '$fakeFieldName' [Access: Pending] [Modification: Pending]\n"
          )
        })
      }
    }
  }
}
