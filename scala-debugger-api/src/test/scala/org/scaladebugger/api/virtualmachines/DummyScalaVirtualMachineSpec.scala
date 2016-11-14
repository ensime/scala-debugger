package org.scaladebugger.api.virtualmachines
import acyclic.file

import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ParallelTestExecution, Matchers, FunSpec}
import org.scaladebugger.api.lowlevel.breakpoints.{PendingBreakpointSupport, DummyBreakpointManager}
import org.scaladebugger.api.lowlevel.classes.{PendingClassUnloadSupport, DummyClassUnloadManager, PendingClassPrepareSupport, DummyClassPrepareManager}
import org.scaladebugger.api.lowlevel.events.{PendingEventHandlerSupport, DummyEventManager}
import org.scaladebugger.api.lowlevel.exceptions.{PendingExceptionSupport, DummyExceptionManager}
import org.scaladebugger.api.lowlevel.methods.{PendingMethodExitSupport, DummyMethodExitManager, PendingMethodEntrySupport, DummyMethodEntryManager}
import org.scaladebugger.api.lowlevel.monitors._
import org.scaladebugger.api.lowlevel.steps.{PendingStepSupport, DummyStepManager}
import org.scaladebugger.api.lowlevel.threads.{PendingThreadStartSupport, DummyThreadStartManager, PendingThreadDeathSupport, DummyThreadDeathManager}
import org.scaladebugger.api.lowlevel.vm.{PendingVMDeathSupport, DummyVMDeathManager}
import org.scaladebugger.api.lowlevel.watchpoints.{PendingModificationWatchpointSupport, DummyModificationWatchpointManager, PendingAccessWatchpointSupport, DummyAccessWatchpointManager}
import org.scaladebugger.api.profiles.ProfileManager

class DummyScalaVirtualMachineSpec extends test.ParallelMockFunSpec
{
  private val mockProfileManager = mock[ProfileManager]
  private val dummyScalaVirtualMachine = new DummyScalaVirtualMachine(
    mockProfileManager
  )

  describe("DummyScalaVirtualMachine") {
    describe("#initialize") {
      it("should do nothing") {
        dummyScalaVirtualMachine.initialize()
      }
    }

    describe("#startProcessingEvents") {
      it("should do nothing") {
        dummyScalaVirtualMachine.startProcessingEvents()
      }
    }

    describe("#stopProcessingEvents") {
      it("should do nothing") {
        dummyScalaVirtualMachine.stopProcessingEvents()
      }
    }

    describe("#isProcessingEvents") {
      it("should return false") {
        val expected = false

        val actual = dummyScalaVirtualMachine.isProcessingEvents

        actual should be (expected)
      }
    }

    describe("#isInitialized") {
      it("should return false") {
        val expected = false

        val actual = dummyScalaVirtualMachine.isInitialized

        actual should be (expected)
      }
    }

    describe("#isStarted") {
      it("should return false") {
        val expected = false

        val actual = dummyScalaVirtualMachine.isStarted

        actual should be (expected)
      }
    }

    describe("#cache") {
      it("should return the same cache instance") {
        val expected = dummyScalaVirtualMachine.cache

        expected should not be (null)

        val actual = dummyScalaVirtualMachine.cache

        actual should be (expected)
      }
    }

    describe("#lowlevel") {
      it("should return a container of dummy managers") {
        val managerContainer = dummyScalaVirtualMachine.lowlevel

        // TODO: Provide a less hard-coded test (this was pulled from manager container spec)
        managerContainer.accessWatchpointManager shouldBe a [DummyAccessWatchpointManager]
        managerContainer.accessWatchpointManager shouldBe a [PendingAccessWatchpointSupport]
        managerContainer.breakpointManager shouldBe a [DummyBreakpointManager]
        managerContainer.breakpointManager shouldBe a [PendingBreakpointSupport]
        managerContainer.classManager should be (null)
        managerContainer.classPrepareManager shouldBe a [DummyClassPrepareManager]
        managerContainer.classPrepareManager shouldBe a [PendingClassPrepareSupport]
        managerContainer.classUnloadManager shouldBe a [DummyClassUnloadManager]
        managerContainer.classUnloadManager shouldBe a [PendingClassUnloadSupport]
        managerContainer.eventManager shouldBe a [DummyEventManager]
        managerContainer.eventManager shouldBe a [PendingEventHandlerSupport]
        managerContainer.exceptionManager shouldBe a [DummyExceptionManager]
        managerContainer.exceptionManager shouldBe a [PendingExceptionSupport]
        managerContainer.methodEntryManager shouldBe a [DummyMethodEntryManager]
        managerContainer.methodEntryManager shouldBe a [PendingMethodEntrySupport]
        managerContainer.methodExitManager shouldBe a [DummyMethodExitManager]
        managerContainer.methodExitManager shouldBe a [PendingMethodExitSupport]
        managerContainer.modificationWatchpointManager shouldBe a [DummyModificationWatchpointManager]
        managerContainer.modificationWatchpointManager shouldBe a [PendingModificationWatchpointSupport]
        managerContainer.monitorContendedEnteredManager shouldBe a [DummyMonitorContendedEnteredManager]
        managerContainer.monitorContendedEnteredManager shouldBe a [PendingMonitorContendedEnteredSupport]
        managerContainer.monitorContendedEnterManager shouldBe a [DummyMonitorContendedEnterManager]
        managerContainer.monitorContendedEnterManager shouldBe a [PendingMonitorContendedEnterSupport]
        managerContainer.monitorWaitedManager shouldBe a [DummyMonitorWaitedManager]
        managerContainer.monitorWaitedManager shouldBe a [PendingMonitorWaitedSupport]
        managerContainer.monitorWaitManager shouldBe a [DummyMonitorWaitManager]
        managerContainer.monitorWaitManager shouldBe a [PendingMonitorWaitSupport]
        managerContainer.requestManager should be (null)
        managerContainer.stepManager shouldBe a [DummyStepManager]
        managerContainer.stepManager shouldBe a [PendingStepSupport]
        managerContainer.threadDeathManager shouldBe a [DummyThreadDeathManager]
        managerContainer.threadDeathManager shouldBe a [PendingThreadDeathSupport]
        managerContainer.threadStartManager shouldBe a [DummyThreadStartManager]
        managerContainer.threadStartManager shouldBe a [PendingThreadStartSupport]
        managerContainer.vmDeathManager shouldBe a [DummyVMDeathManager]
        managerContainer.vmDeathManager shouldBe a [PendingVMDeathSupport]
      }

      it("should return the same container each time") {
        val expected = dummyScalaVirtualMachine.lowlevel

        val actual = dummyScalaVirtualMachine.lowlevel

        actual should be (expected)
      }
    }

    describe("#uniqueId") {
      it("should return a non-empty string") {
        dummyScalaVirtualMachine.uniqueId should not be (empty)
      }

      it("should return the same id each time") {
        val expected = dummyScalaVirtualMachine.uniqueId

        val actual = dummyScalaVirtualMachine.uniqueId

        actual should be (expected)
      }
    }

    describe("#underlyingVirtualMachine") {
      it("should return null") {
        val expected = null

        val actual = dummyScalaVirtualMachine.underlyingVirtualMachine

        actual should be (expected)
      }
    }

    describe("#resume") {
      it("should do nothing") {
        dummyScalaVirtualMachine.resume()
      }
    }

    describe("#suspend") {
      it("should do nothing") {
        dummyScalaVirtualMachine.suspend()
      }
    }

    describe("#register") {
      it("should invoke the underlying profile manager") {
        val testName = "some name"
        val testProfile = mock[DebugProfile]
        val expected = Some(testProfile)

        (mockProfileManager.register _).expects(testName, testProfile)
          .returning(expected).once()

        val actual = dummyScalaVirtualMachine.register(testName, testProfile)
        actual should be (expected)
      }
    }

    describe("#unregister") {
      it("should invoke the underlying profile manager") {
        val testName = "some name"
        val testProfile = mock[DebugProfile]
        val expected = Some(testProfile)

        (mockProfileManager.unregister _).expects(testName)
          .returning(expected).once()

        val actual = dummyScalaVirtualMachine.unregister(testName)
        actual should be (expected)
      }
    }

    describe("#retrieve") {
      it("should invoke the underlying profile manager") {
        val testName = "some name"
        val testProfile = mock[DebugProfile]
        val expected = Some(testProfile)

        (mockProfileManager.retrieve _).expects(testName)
          .returning(expected).once()

        val actual = dummyScalaVirtualMachine.retrieve(testName)
        actual should be (expected)
      }
    }
  }
}
