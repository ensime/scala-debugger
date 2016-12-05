package org.scaladebugger.api.virtualmachines

import com.sun.jdi.VirtualMachine
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.profiles.ProfileManager

class ScalaVirtualMachineSpec extends test.ParallelMockFunSpec
{
  private class TestManagerContainer extends ManagerContainer(
    null, null, null, null, null, null, null, null, null, null, null, null,
    null, null, null, null, null, null, null
  )

  private def newScalaVirtualMachine(
    scalaVirtualMachineManager: ScalaVirtualMachineManager,
    managerContainer: ManagerContainer,
    _profileManager: ProfileManager
  ) =
    new Object with ScalaVirtualMachine {
      override val cache: ObjectCache = null
      override val lowlevel: ManagerContainer = managerContainer
      override val manager: ScalaVirtualMachineManager = scalaVirtualMachineManager
      override def startProcessingEvents(): Unit = {}
      override def isInitialized: Boolean = false
      override def isProcessingEvents: Boolean = false
      override def suspend(): Unit = {}
      override def stopProcessingEvents(): Unit = {}
      override def resume(): Unit = {}
      override def initialize(
        defaultProfile: String,
        startProcessingEvents: Boolean
      ): Unit = {}
      override val underlyingVirtualMachine: VirtualMachine = null
      override def isStarted: Boolean = false
      override val uniqueId: String = ""
      override protected val profileManager: ProfileManager = _profileManager
      override def register(name: String, profile: DebugProfile): Option[DebugProfile] = None
      override def retrieve(name: String): Option[DebugProfile] = None
      override def unregister(name: String): Option[DebugProfile] = None
    }

  private val mockManagerContainerProcessPendingRequests =
    mockFunction[ManagerContainer, Unit]
  private val mockProfileManager = mock[ProfileManager]
  private val scalaVirtualMachine = newScalaVirtualMachine(
    ScalaVirtualMachineManager.GlobalInstance,
    new TestManagerContainer {
      override def processPendingRequests(
        managerContainer: ManagerContainer
      ): Unit = mockManagerContainerProcessPendingRequests(managerContainer)
    },
    mockProfileManager
  )

  describe("ScalaVirtualMachine") {
    describe("#processPendingRequests") {
      it("should process the other VM's pending requests using its low-level managers") {
        val otherManagerContainer = new TestManagerContainer
        val otherScalaVirtualMachine = newScalaVirtualMachine(
          null, otherManagerContainer, null
        )

        mockManagerContainerProcessPendingRequests.expects(otherManagerContainer).once()

        scalaVirtualMachine.processPendingRequests(otherScalaVirtualMachine)
      }
    }
  }
}
