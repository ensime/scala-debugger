package org.scaladebugger.tool.commands

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.Eventually
import test.{ToolConstants, ToolFixtures, ToolTestUtilities}

class UnwatchAccessCommandIntegrationSpec extends ParallelMockFunSpec
  with ToolFixtures
  with ToolTestUtilities
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(ToolConstants.EventuallyTimeout),
    interval = scaled(ToolConstants.EventuallyInterval)
  )

  describe("UnwatchBothCommand") {
    it("should delete a specific pending access watchpoint request by class and field name") {
      val testClass = "org.scaladebugger.test.watchpoints.AccessWatchpoint"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val className = "org.scaladebugger.test.watchpoints.SomeAccessClass"
      val fieldName = "field"

      val fakeClassName = "org.invalid.class"
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

          // Remove the pending request
          vt.newInputLine(s"unwatcha $q$fakeClassName$q $q$fakeFieldName$q")

          // Verify our pending watch request was removed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head

            val awrs = svm.accessWatchpointRequests.map(awr =>
              (awr.className, awr.fieldName, awr.isPending))
            awrs should contain theSameElementsAs Seq(
              (className, fieldName, false)
            )

            val mwrs = svm.modificationWatchpointRequests.map(awr =>
              (awr.className, awr.fieldName, awr.isPending))
            mwrs should contain theSameElementsAs Seq(
              (className, fieldName, false),
              (fakeClassName, fakeFieldName, true)
            )
          }
        })
      }
    }

    it("should delete a specific active access watchpoint request by class and field name") {
      val testClass = "org.scaladebugger.test.watchpoints.AccessWatchpoint"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val className = "org.scaladebugger.test.watchpoints.SomeAccessClass"
      val fieldName = "field"

      val fakeClassName = "org.invalid.class"
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

          // Remove the active request
          vt.newInputLine(s"unwatcha $q$className$q $q$fieldName$q")

          // Verify our active watch request was removed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head

            val awrs = svm.accessWatchpointRequests.map(awr =>
              (awr.className, awr.fieldName, awr.isPending))
            awrs should contain theSameElementsAs Seq(
              (fakeClassName, fakeFieldName, true)
            )

            val mwrs = svm.modificationWatchpointRequests.map(awr =>
              (awr.className, awr.fieldName, awr.isPending))
            mwrs should contain theSameElementsAs Seq(
              (className, fieldName, false),
              (fakeClassName, fakeFieldName, true)
            )
          }
        })
      }
    }

    it("should delete all pending and active access watchpoint requests matching a class wildcard") {
      val testClass = "org.scaladebugger.test.watchpoints.AccessWatchpoint"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val className = "org.scaladebugger.test.watchpoints.SomeAccessClass"
      val fieldName = "field"

      val fakeClassName = "org.invalid.class"
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

          // Remove requests matching the wildcard
          vt.newInputLine("unwatcha \"org.*\"")

          // Verify our watch requests were removed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head

            val awrs = svm.accessWatchpointRequests.map(awr =>
              (awr.className, awr.fieldName, awr.isPending))
            awrs should be (empty)

            val mwrs = svm.modificationWatchpointRequests.map(awr =>
              (awr.className, awr.fieldName, awr.isPending))
            mwrs should contain theSameElementsAs Seq(
              (className, fieldName, false),
              (fakeClassName, fakeFieldName, true)
            )
          }
        })
      }
    }

    it("should delete all pending and active access watchpoint requests if no args provided") {
      val testClass = "org.scaladebugger.test.watchpoints.AccessWatchpoint"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val className = "org.scaladebugger.test.watchpoints.SomeAccessClass"
      val fieldName = "field"

      val fakeClassName = "org.invalid.class"
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

          // Remove all requests
          vt.newInputLine("unwatcha")

          // Verify our watch requests were removed
          eventually {
            val svm = sm.state.scalaVirtualMachines.head

            val awrs = svm.accessWatchpointRequests.map(awr =>
              (awr.className, awr.fieldName, awr.isPending))
            awrs should be (empty)

            val mwrs = svm.modificationWatchpointRequests.map(awr =>
              (awr.className, awr.fieldName, awr.isPending))
            mwrs should contain theSameElementsAs Seq(
              (className, fieldName, false),
              (fakeClassName, fakeFieldName, true)
            )
          }
        })
      }
    }
  }
}
