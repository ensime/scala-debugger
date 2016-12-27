package org.scaladebugger.tool.commands

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class WatchListCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
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
      virtualTerminal.newInputLine(s"watcha $q$className$q $q$fieldName$q")
      virtualTerminal.newInputLine(s"watchm $q$className$q $q$fieldName$q")
      virtualTerminal.newInputLine(s"watch $q$fakeClassName$q $q$fakeFieldName$q")
      virtualTerminal.newInputLine(s"watcha $q$fakeClassName$q $q$fakeFieldName$q")
      virtualTerminal.newInputLine(s"watchm $q$fakeClassName$q $q$fakeFieldName$q")

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
              (className, fieldName, false),
              (fakeClassName, fakeFieldName, true),
              (fakeClassName, fakeFieldName, true)
            )

            val mwrs = svm.modificationWatchpointRequests.map(awr =>
              (awr.className, awr.fieldName, awr.isPending))
            mwrs should contain theSameElementsAs Seq(
              (className, fieldName, false),
              (className, fieldName, false),
              (fakeClassName, fakeFieldName, true),
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
