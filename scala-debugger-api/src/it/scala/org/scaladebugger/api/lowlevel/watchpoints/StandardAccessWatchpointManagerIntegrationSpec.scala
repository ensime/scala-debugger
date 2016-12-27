package org.scaladebugger.api.lowlevel.watchpoints

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.AccessWatchpointEvent
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class StandardAccessWatchpointManagerIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("StandardAccessWatchpointManager") {
    it("should be able to detect access to a field") {
      val testClass = "org.scaladebugger.test.watchpoints.AccessWatchpoint"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val className = "org.scaladebugger.test.watchpoints.SomeAccessClass"
      val fieldName = "field"

      val detectedAccessWatchpoint = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      accessWatchpointManager.createAccessWatchpointRequest(
        className,
        fieldName
      )

      // Listen for access watchpoint events for specific variable
      eventManager.addResumingEventHandler(AccessWatchpointEventType, e => {
        val accessWatchpointEvent = e.asInstanceOf[AccessWatchpointEvent]
        val name = accessWatchpointEvent.field().name()

        // If we detected access for our variable, mark our flag
        if (name == fieldName) detectedAccessWatchpoint.set(true)
      })

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          assert(detectedAccessWatchpoint.get(), s"$fieldName never accessed!")
        })
      }
    }
  }
}
