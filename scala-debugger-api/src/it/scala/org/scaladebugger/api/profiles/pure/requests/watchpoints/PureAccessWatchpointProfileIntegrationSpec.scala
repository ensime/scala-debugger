package org.scaladebugger.api.profiles.pure.requests.watchpoints

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.AccessWatchpointEvent
import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import test.{TestUtilities, VirtualMachineFixtures}

class PureAccessWatchpointProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureAccessWatchpointProfile") {
    it("should be able to detect access to a field") {
      val testClass = "org.scaladebugger.test.watchpoints.AccessWatchpoint"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val className = "org.scaladebugger.test.watchpoints.SomeAccessClass"
      val fieldName = "field"

      val detectedAccessWatchpoint = new AtomicBoolean(false)

      val s = DummyScalaVirtualMachine.newInstance()

      // Listen for access watchpoint events for specific variable
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateAccessWatchpointRequest(className, fieldName)
        .filter(_.field.declaringTypeInfo.name == className)
        .filter(_.field.name == fieldName)
        .foreach(_ => detectedAccessWatchpoint.set(true))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          assert(detectedAccessWatchpoint.get(), s"$fieldName never accessed!")
        })
      }
    }
  }
}
