package org.scaladebugger.api.profiles.pure.requests.watchpoints

import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class PureModificationWatchpointRequestIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("PureModificationWatchpointRequest") {
    it("should be able to detect modification to a field") {
      val testClass = "org.scaladebugger.test.watchpoints.ModificationWatchpoint"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val className = "org.scaladebugger.test.watchpoints.SomeModificationClass"
      val fieldName = "field"

      val detectedModificationWatchpoint = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      // Listen for modification watchpoint events for specific variable
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateModificationWatchpointRequest(className, fieldName)
        .filter(_.field.declaringTypeInfo.name == className)
        .filter(_.field.name == fieldName)
        .foreach(_ => detectedModificationWatchpoint.set(true))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          assert(detectedModificationWatchpoint.get(), s"$fieldName never modified!")
        })
      }
    }
  }
}
