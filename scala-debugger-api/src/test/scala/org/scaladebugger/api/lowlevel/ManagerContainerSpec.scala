package org.scaladebugger.api.lowlevel

import com.sun.jdi.ThreadReference
import com.sun.jdi.request.EventRequestManager
import org.scaladebugger.api.lowlevel.breakpoints._
import org.scaladebugger.api.lowlevel.classes._
import org.scaladebugger.api.lowlevel.events.EventManager.EventHandler
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.lowlevel.events._
import org.scaladebugger.api.lowlevel.exceptions._
import org.scaladebugger.api.lowlevel.methods._
import org.scaladebugger.api.lowlevel.monitors._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.steps._
import org.scaladebugger.api.lowlevel.threads._
import org.scaladebugger.api.lowlevel.vm._
import org.scaladebugger.api.lowlevel.watchpoints._
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.PendingManagers._

class ManagerContainerSpec extends ParallelMockFunSpec {
  describe("ManagerContainer") {
    describe("#processPendingRequests") {
      it("should create breakpoint requests if some are pending") {
        val info = BreakpointRequestInfo(
          requestId = java.util.UUID.randomUUID().toString,
          isPending = true,
          fileName = "some/file/name",
          lineNumber = 999,
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.breakpointManager
          .createBreakpointRequestFromInfo(info)

        (managerContainerWithMocks.breakpointManager.createBreakpointRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }

      it("should create class prepare requests if some are pending") {
        val info = ClassPrepareRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.classPrepareManager
          .createClassPrepareRequestFromInfo(info)

        (managerContainerWithMocks.classPrepareManager.createClassPrepareRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }

      it("should create class unload requests if some are pending") {
        val info = ClassUnloadRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.classUnloadManager
          .createClassUnloadRequestFromInfo(info)

        (managerContainerWithMocks.classUnloadManager.createClassUnloadRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }
      
      it("should add event handlers if some are pending") {
        val info = EventHandlerInfo(
          eventHandlerId = java.util.UUID.randomUUID().toString,
          eventType = stub[EventType],
          eventHandler = stub[EventHandler],
          Seq(mock[JDIEventArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.eventManager
          .addEventHandlerFromInfo(info)

        (managerContainerWithMocks.eventManager.addEventHandlerFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }
      
      it("should create exception requests if some are pending") {
        val info = ExceptionRequestInfo(
          requestId = java.util.UUID.randomUUID().toString,
          isPending = true,
          className = "some.class.name",
          notifyCaught = true,
          notifyUncaught = false,
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.exceptionManager
          .createExceptionRequestFromInfo(info)

        (managerContainerWithMocks.exceptionManager.createExceptionRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }
      
      it("should create method entry requests if some are pending") {
        val info = MethodEntryRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          "some.class.name",
          "someMethodName",
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.methodEntryManager
          .createMethodEntryRequestFromInfo(info)

        (managerContainerWithMocks.methodEntryManager.createMethodEntryRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }
      
      it("should create method exit requests if some are pending") {
        val info = MethodExitRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          "some.class.name",
          "someMethodName",
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.methodExitManager
          .createMethodExitRequestFromInfo(info)

        (managerContainerWithMocks.methodExitManager.createMethodExitRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }
      
      it("should create monitor contended entered requests if some are pending") {
        val info = MonitorContendedEnteredRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.monitorContendedEnteredManager
          .createMonitorContendedEnteredRequestFromInfo(info)

        (managerContainerWithMocks.monitorContendedEnteredManager.createMonitorContendedEnteredRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }
      
      it("should create monitor contended enter requests if some are pending") {
        val info = MonitorContendedEnterRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.monitorContendedEnterManager
          .createMonitorContendedEnterRequestFromInfo(info)

        (managerContainerWithMocks.monitorContendedEnterManager.createMonitorContendedEnterRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }

      it("should create monitor waited requests if some are pending") {
        val info = MonitorWaitedRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.monitorWaitedManager
          .createMonitorWaitedRequestFromInfo(info)

        (managerContainerWithMocks.monitorWaitedManager.createMonitorWaitedRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }

      it("should create monitor wait requests if some are pending") {
        val info = MonitorWaitRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.monitorWaitManager
          .createMonitorWaitRequestFromInfo(info)

        (managerContainerWithMocks.monitorWaitManager.createMonitorWaitRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }
      
      it("should create step requests if some are pending") {
        val info = StepRequestInfo(
          requestId = java.util.UUID.randomUUID().toString,
          isPending = true,
          removeExistingRequests = false,
          threadReference = mock[ThreadReference],
          size = 0,
          depth = 1,
          extraArguments = Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.stepManager
          .createStepRequestFromInfo(info)

        (managerContainerWithMocks.stepManager.createStepRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }

      it("should create thread death requests if some are pending") {
        val info = ThreadDeathRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.threadDeathManager
          .createThreadDeathRequestFromInfo(info)

        (managerContainerWithMocks.threadDeathManager.createThreadDeathRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }

      it("should create thread start requests if some are pending") {
        val info = ThreadStartRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.threadStartManager
          .createThreadStartRequestFromInfo(info)

        (managerContainerWithMocks.threadStartManager.createThreadStartRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }

      it("should create vm death requests if some are pending") {
        val info = VMDeathRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.vmDeathManager
          .createVMDeathRequestFromInfo(info)

        (managerContainerWithMocks.vmDeathManager.createVMDeathRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }

      it("should create access watchpoint requests if some are pending") {
        val info = AccessWatchpointRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          "some.class.name",
          "someFieldName",
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.accessWatchpointManager
          .createAccessWatchpointRequestFromInfo(info)

        (managerContainerWithMocks.accessWatchpointManager.createAccessWatchpointRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }

      it("should create modification watchpoint requests if some are pending") {
        val info = ModificationWatchpointRequestInfo(
          java.util.UUID.randomUUID().toString,
          isPending = true,
          "some.class.name",
          "someFieldName",
          Seq(mock[JDIRequestArgument])
        )
        val dummyManagerContainer = ManagerContainer.usingDummyManagers()
        dummyManagerContainer.modificationWatchpointManager
          .createModificationWatchpointRequestFromInfo(info)

        (managerContainerWithMocks.modificationWatchpointManager.createModificationWatchpointRequestFromInfo _)
          .expects(info).once()

        managerContainerWithMocks.processPendingRequests(dummyManagerContainer)
      }
    }

    describe("#usingDummyManagers") {
      it("should create dummy managers supporting pending requests") {
        val managerContainer = ManagerContainer.usingDummyManagers()

        managerContainer.accessWatchpointManager shouldBe a [DummyAccessWatchpointManager]
        managerContainer.accessWatchpointManager shouldBe a [PendingAccessWatchpointSupportLike]
        managerContainer.breakpointManager shouldBe a [DummyBreakpointManager]
        managerContainer.breakpointManager shouldBe a [PendingBreakpointSupportLike]
        managerContainer.classManager should be (null)
        managerContainer.classPrepareManager shouldBe a [DummyClassPrepareManager]
        managerContainer.classPrepareManager shouldBe a [PendingClassPrepareSupportLike]
        managerContainer.classUnloadManager shouldBe a [DummyClassUnloadManager]
        managerContainer.classUnloadManager shouldBe a [PendingClassUnloadSupportLike]
        managerContainer.eventManager shouldBe a [DummyEventManager]
        managerContainer.eventManager shouldBe a [PendingEventHandlerSupportLike]
        managerContainer.exceptionManager shouldBe a [DummyExceptionManager]
        managerContainer.exceptionManager shouldBe a [PendingExceptionSupportLike]
        managerContainer.methodEntryManager shouldBe a [DummyMethodEntryManager]
        managerContainer.methodEntryManager shouldBe a [PendingMethodEntrySupportLike]
        managerContainer.methodExitManager shouldBe a [DummyMethodExitManager]
        managerContainer.methodExitManager shouldBe a [PendingMethodExitSupportLike]
        managerContainer.modificationWatchpointManager shouldBe a [DummyModificationWatchpointManager]
        managerContainer.modificationWatchpointManager shouldBe a [PendingModificationWatchpointSupportLike]
        managerContainer.monitorContendedEnteredManager shouldBe a [DummyMonitorContendedEnteredManager]
        managerContainer.monitorContendedEnteredManager shouldBe a [PendingMonitorContendedEnteredSupportLike]
        managerContainer.monitorContendedEnterManager shouldBe a [DummyMonitorContendedEnterManager]
        managerContainer.monitorContendedEnterManager shouldBe a [PendingMonitorContendedEnterSupportLike]
        managerContainer.monitorWaitedManager shouldBe a [DummyMonitorWaitedManager]
        managerContainer.monitorWaitedManager shouldBe a [PendingMonitorWaitedSupportLike]
        managerContainer.monitorWaitManager shouldBe a [DummyMonitorWaitManager]
        managerContainer.monitorWaitManager shouldBe a [PendingMonitorWaitSupportLike]
        managerContainer.requestManager should be (null)
        managerContainer.stepManager shouldBe a [DummyStepManager]
        managerContainer.stepManager shouldBe a [PendingStepSupportLike]
        managerContainer.threadDeathManager shouldBe a [DummyThreadDeathManager]
        managerContainer.threadDeathManager shouldBe a [PendingThreadDeathSupportLike]
        managerContainer.threadStartManager shouldBe a [DummyThreadStartManager]
        managerContainer.threadStartManager shouldBe a [PendingThreadStartSupportLike]
        managerContainer.vmDeathManager shouldBe a [DummyVMDeathManager]
        managerContainer.vmDeathManager shouldBe a [PendingVMDeathSupportLike]
      }
    }
    
    describe("#enablePendingSupport") {
      it("should enable pending support for any manager supporting pending requests") {
        val mockAccessWatchpointManager = mock[TestPendingAccessWatchpointManager]
        val mockBreakpointManager = mock[TestPendingBreakpointManager]
        val mockClassManager = mock[TestPendingClassManager]
        val mockClassPrepareManager = mock[TestPendingClassPrepareManager]
        val mockClassUnloadManager = mock[TestPendingClassUnloadManager]
        val mockEventManager = mock[TestPendingEventManager]
        val mockExceptionManager = mock[TestPendingExceptionManager]
        val mockMethodEntryManager = mock[TestPendingMethodEntryManager]
        val mockMethodExitManager = mock[TestPendingMethodExitManager]
        val mockModificationWatchpointManager = mock[TestPendingModificationWatchpointManager]
        val mockMonitorContendedEnteredManager = mock[TestPendingMonitorContendedEnteredManager]
        val mockMonitorContendedEnterManager = mock[TestPendingMonitorContendedEnterManager]
        val mockMonitorWaitedManager = mock[TestPendingMonitorWaitedManager]
        val mockMonitorWaitManager = mock[TestPendingMonitorWaitManager]
        val mockRequestManager = mock[TestPendingEventRequestManager]
        val mockStepManager = mock[TestPendingStepManager]
        val mockThreadDeathManager = mock[TestPendingThreadDeathManager]
        val mockThreadStartManager = mock[TestPendingThreadStartManager]
        val mockVMDeathManager = mock[TestPendingVMDeathManager]
        
        val managerContainer = ManagerContainer(
          mockAccessWatchpointManager,
          mockBreakpointManager,
          mockClassManager,
          mockClassPrepareManager,
          mockClassUnloadManager,
          mockEventManager,
          mockExceptionManager,
          mockMethodEntryManager,
          mockMethodExitManager,
          mockModificationWatchpointManager,
          mockMonitorContendedEnteredManager,
          mockMonitorContendedEnterManager,
          mockMonitorWaitedManager,
          mockMonitorWaitManager,
          mockRequestManager,
          mockStepManager,
          mockThreadDeathManager,
          mockThreadStartManager,
          mockVMDeathManager
        )

        (mockAccessWatchpointManager.setPendingSupport _).expects(true).once()
        (mockBreakpointManager.setPendingSupport _).expects(true).once()
        (mockClassManager.setPendingSupport _).expects(true).once()
        (mockClassPrepareManager.setPendingSupport _).expects(true).once()
        (mockClassUnloadManager.setPendingSupport _).expects(true).once()
        (mockEventManager.setPendingSupport _).expects(true).once()
        (mockExceptionManager.setPendingSupport _).expects(true).once()
        (mockMethodEntryManager.setPendingSupport _).expects(true).once()
        (mockMethodExitManager.setPendingSupport _).expects(true).once()
        (mockModificationWatchpointManager.setPendingSupport _).expects(true).once()
        (mockMonitorContendedEnteredManager.setPendingSupport _).expects(true).once()
        (mockMonitorContendedEnterManager.setPendingSupport _).expects(true).once()
        (mockMonitorWaitedManager.setPendingSupport _).expects(true).once()
        (mockMonitorWaitManager.setPendingSupport _).expects(true).once()
        (mockRequestManager.setPendingSupport _).expects(true).once()
        (mockStepManager.setPendingSupport _).expects(true).once()
        (mockThreadDeathManager.setPendingSupport _).expects(true).once()
        (mockThreadStartManager.setPendingSupport _).expects(true).once()
        (mockVMDeathManager.setPendingSupport _).expects(true).once()

        managerContainer.enablePendingSupport()
      }

      it("should do nothing for any manager not supporting pending requests") {
        val mockAccessWatchpointManager = mock[AccessWatchpointManager]
        val mockBreakpointManager = mock[BreakpointManager]
        val mockClassManager = mock[ClassManager]
        val mockClassPrepareManager = mock[ClassPrepareManager]
        val mockClassUnloadManager = mock[ClassUnloadManager]
        val mockEventManager = mock[EventManager]
        val mockExceptionManager = mock[ExceptionManager]
        val mockMethodEntryManager = mock[MethodEntryManager]
        val mockMethodExitManager = mock[MethodExitManager]
        val mockModificationWatchpointManager = mock[ModificationWatchpointManager]
        val mockMonitorContendedEnteredManager = mock[MonitorContendedEnteredManager]
        val mockMonitorContendedEnterManager = mock[MonitorContendedEnterManager]
        val mockMonitorWaitedManager = mock[MonitorWaitedManager]
        val mockMonitorWaitManager = mock[MonitorWaitManager]
        val mockRequestManager = mock[EventRequestManager]
        val mockStepManager = mock[StepManager]
        val mockThreadDeathManager = mock[ThreadDeathManager]
        val mockThreadStartManager = mock[ThreadStartManager]
        val mockVMDeathManager = mock[VMDeathManager]

        val managerContainer = ManagerContainer(
          mockAccessWatchpointManager,
          mockBreakpointManager,
          mockClassManager,
          mockClassPrepareManager,
          mockClassUnloadManager,
          mockEventManager,
          mockExceptionManager,
          mockMethodEntryManager,
          mockMethodExitManager,
          mockModificationWatchpointManager,
          mockMonitorContendedEnteredManager,
          mockMonitorContendedEnterManager,
          mockMonitorWaitedManager,
          mockMonitorWaitManager,
          mockRequestManager,
          mockStepManager,
          mockThreadDeathManager,
          mockThreadStartManager,
          mockVMDeathManager
        )

        managerContainer.enablePendingSupport()
      }
    }

    describe("#disablePendingSupport") {
      it("should disable pending support for any manager supporting pending requests") {
        val mockAccessWatchpointManager = mock[TestPendingAccessWatchpointManager]
        val mockBreakpointManager = mock[TestPendingBreakpointManager]
        val mockClassManager = mock[TestPendingClassManager]
        val mockClassPrepareManager = mock[TestPendingClassPrepareManager]
        val mockClassUnloadManager = mock[TestPendingClassUnloadManager]
        val mockEventManager = mock[TestPendingEventManager]
        val mockExceptionManager = mock[TestPendingExceptionManager]
        val mockMethodEntryManager = mock[TestPendingMethodEntryManager]
        val mockMethodExitManager = mock[TestPendingMethodExitManager]
        val mockModificationWatchpointManager = mock[TestPendingModificationWatchpointManager]
        val mockMonitorContendedEnteredManager = mock[TestPendingMonitorContendedEnteredManager]
        val mockMonitorContendedEnterManager = mock[TestPendingMonitorContendedEnterManager]
        val mockMonitorWaitedManager = mock[TestPendingMonitorWaitedManager]
        val mockMonitorWaitManager = mock[TestPendingMonitorWaitManager]
        val mockRequestManager = mock[TestPendingEventRequestManager]
        val mockStepManager = mock[TestPendingStepManager]
        val mockThreadDeathManager = mock[TestPendingThreadDeathManager]
        val mockThreadStartManager = mock[TestPendingThreadStartManager]
        val mockVMDeathManager = mock[TestPendingVMDeathManager]

        val managerContainer = ManagerContainer(
          mockAccessWatchpointManager,
          mockBreakpointManager,
          mockClassManager,
          mockClassPrepareManager,
          mockClassUnloadManager,
          mockEventManager,
          mockExceptionManager,
          mockMethodEntryManager,
          mockMethodExitManager,
          mockModificationWatchpointManager,
          mockMonitorContendedEnteredManager,
          mockMonitorContendedEnterManager,
          mockMonitorWaitedManager,
          mockMonitorWaitManager,
          mockRequestManager,
          mockStepManager,
          mockThreadDeathManager,
          mockThreadStartManager,
          mockVMDeathManager
        )

        (mockAccessWatchpointManager.setPendingSupport _).expects(false).once()
        (mockBreakpointManager.setPendingSupport _).expects(false).once()
        (mockClassManager.setPendingSupport _).expects(false).once()
        (mockClassPrepareManager.setPendingSupport _).expects(false).once()
        (mockClassUnloadManager.setPendingSupport _).expects(false).once()
        (mockEventManager.setPendingSupport _).expects(false).once()
        (mockExceptionManager.setPendingSupport _).expects(false).once()
        (mockMethodEntryManager.setPendingSupport _).expects(false).once()
        (mockMethodExitManager.setPendingSupport _).expects(false).once()
        (mockModificationWatchpointManager.setPendingSupport _).expects(false).once()
        (mockMonitorContendedEnteredManager.setPendingSupport _).expects(false).once()
        (mockMonitorContendedEnterManager.setPendingSupport _).expects(false).once()
        (mockMonitorWaitedManager.setPendingSupport _).expects(false).once()
        (mockMonitorWaitManager.setPendingSupport _).expects(false).once()
        (mockRequestManager.setPendingSupport _).expects(false).once()
        (mockStepManager.setPendingSupport _).expects(false).once()
        (mockThreadDeathManager.setPendingSupport _).expects(false).once()
        (mockThreadStartManager.setPendingSupport _).expects(false).once()
        (mockVMDeathManager.setPendingSupport _).expects(false).once()

        managerContainer.disablePendingSupport()
      }

      it("should do nothing for any manager not supporting pending requests") {
        val mockAccessWatchpointManager = mock[AccessWatchpointManager]
        val mockBreakpointManager = mock[BreakpointManager]
        val mockClassManager = mock[ClassManager]
        val mockClassPrepareManager = mock[ClassPrepareManager]
        val mockClassUnloadManager = mock[ClassUnloadManager]
        val mockEventManager = mock[EventManager]
        val mockExceptionManager = mock[ExceptionManager]
        val mockMethodEntryManager = mock[MethodEntryManager]
        val mockMethodExitManager = mock[MethodExitManager]
        val mockModificationWatchpointManager = mock[ModificationWatchpointManager]
        val mockMonitorContendedEnteredManager = mock[MonitorContendedEnteredManager]
        val mockMonitorContendedEnterManager = mock[MonitorContendedEnterManager]
        val mockMonitorWaitedManager = mock[MonitorWaitedManager]
        val mockMonitorWaitManager = mock[MonitorWaitManager]
        val mockRequestManager = mock[EventRequestManager]
        val mockStepManager = mock[StepManager]
        val mockThreadDeathManager = mock[ThreadDeathManager]
        val mockThreadStartManager = mock[ThreadStartManager]
        val mockVMDeathManager = mock[VMDeathManager]

        val managerContainer = ManagerContainer(
          mockAccessWatchpointManager,
          mockBreakpointManager,
          mockClassManager,
          mockClassPrepareManager,
          mockClassUnloadManager,
          mockEventManager,
          mockExceptionManager,
          mockMethodEntryManager,
          mockMethodExitManager,
          mockModificationWatchpointManager,
          mockMonitorContendedEnteredManager,
          mockMonitorContendedEnterManager,
          mockMonitorWaitedManager,
          mockMonitorWaitManager,
          mockRequestManager,
          mockStepManager,
          mockThreadDeathManager,
          mockThreadStartManager,
          mockVMDeathManager
        )

        managerContainer.disablePendingSupport()
      }
    }
  }
  
  private val managerContainerWithMocks = ManagerContainer(
    mock[AccessWatchpointManager],
    mock[BreakpointManager],
    mock[ClassManager],
    mock[ClassPrepareManager],
    mock[ClassUnloadManager],
    mock[EventManager],
    mock[ExceptionManager],
    mock[MethodEntryManager],
    mock[MethodExitManager],
    mock[ModificationWatchpointManager],
    mock[MonitorContendedEnteredManager],
    mock[MonitorContendedEnterManager],
    mock[MonitorWaitedManager],
    mock[MonitorWaitManager],
    mock[EventRequestManager],
    mock[StepManager],
    mock[ThreadDeathManager],
    mock[ThreadStartManager],
    mock[VMDeathManager]
  )

}
