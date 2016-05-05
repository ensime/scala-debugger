package org.scaladebugger.api.lowlevel
import acyclic.file
import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.EventRequestManager
import org.scaladebugger.api.lowlevel.breakpoints._
import org.scaladebugger.api.lowlevel.classes._
import org.scaladebugger.api.lowlevel.events._
import org.scaladebugger.api.lowlevel.exceptions._
import org.scaladebugger.api.lowlevel.methods._
import org.scaladebugger.api.lowlevel.monitors._
import org.scaladebugger.api.lowlevel.steps._
import org.scaladebugger.api.lowlevel.threads._
import org.scaladebugger.api.lowlevel.vm._
import org.scaladebugger.api.lowlevel.watchpoints._
import org.scaladebugger.api.utils.{Logging, LoopingTaskRunner}
import org.slf4j.LoggerFactory

import scala.util.Try

/**
 * Represents a container for low-level managers.
 */
case class ManagerContainer(
  accessWatchpointManager: AccessWatchpointManager,
  breakpointManager: BreakpointManager,
  classManager: ClassManager,
  classPrepareManager: ClassPrepareManager,
  classUnloadManager: ClassUnloadManager,
  eventManager: EventManager,
  exceptionManager: ExceptionManager,
  methodEntryManager: MethodEntryManager,
  methodExitManager: MethodExitManager,
  modificationWatchpointManager: ModificationWatchpointManager,
  monitorContendedEnteredManager: MonitorContendedEnteredManager,
  monitorContendedEnterManager: MonitorContendedEnterManager,
  monitorWaitedManager: MonitorWaitedManager,
  monitorWaitManager: MonitorWaitManager,
  requestManager: EventRequestManager,
  stepManager: StepManager,
  threadDeathManager: ThreadDeathManager,
  threadStartManager: ThreadStartManager,
  vmDeathManager: VMDeathManager
) {
  // NOTE: Name prefixed with underscore to avoid import conflict with logger
  //       trait in Scala 2.10
  private val _logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Processes any pending requests in the managers of this manager container.
   *
   * @note This will remove the pending requests from the managers
   *       contained in this manager container!
   */
  def processPendingRequests(): Unit = synchronized {
    this.productIterator.foreach(m => Try(m match {
      case accessWatchpointManager: PendingAccessWatchpointSupportLike =>
        accessWatchpointManager.pendingAccessWatchpointRequests.foreach(i => {
          _logger.trace(s"Adding access watchpoint request from pending $i")
          accessWatchpointManager.removeAccessWatchpointRequestWithId(i.requestId)
          accessWatchpointManager.createAccessWatchpointRequestFromInfo(i)
        })
      case breakpointManager: PendingBreakpointSupportLike =>
        breakpointManager.pendingBreakpointRequests.foreach(i => {
          _logger.trace(s"Adding breakpoint request from pending $i")
          breakpointManager.removeBreakpointRequestWithId(i.requestId)
          breakpointManager.createBreakpointRequestFromInfo(i)
        })
      case classPrepareManager: PendingClassPrepareSupportLike =>
        classPrepareManager.pendingClassPrepareRequests.foreach(i => {
          _logger.trace(s"Adding class prepare request from pending $i")
          classPrepareManager.removeClassPrepareRequest(i.requestId)
          classPrepareManager.createClassPrepareRequestFromInfo(i)
        })
      case classUnloadManager: PendingClassUnloadSupportLike =>
        classUnloadManager.pendingClassUnloadRequests.foreach(i => {
          _logger.trace(s"Adding class unload request from pending $i")
          classUnloadManager.removeClassUnloadRequest(i.requestId)
          classUnloadManager.createClassUnloadRequestFromInfo(i)
        })
      case eventManager: PendingEventHandlerSupportLike =>
        eventManager.pendingEventHandlers.foreach(i => {
          _logger.trace(s"Adding event handler from pending $i")
          eventManager.removeEventHandler(i.eventHandlerId)
          eventManager.addEventHandlerFromInfo(i)
        })
      case exceptionManager: PendingExceptionSupportLike =>
        exceptionManager.pendingExceptionRequests.foreach(i => {
          _logger.trace(s"Adding exception request from pending $i")
          exceptionManager.removeExceptionRequestWithId(i.requestId)
          exceptionManager.createExceptionRequestFromInfo(i)
        })
      case methodEntryManager: PendingMethodEntrySupportLike =>
        methodEntryManager.pendingMethodEntryRequests.foreach(i => {
          _logger.trace(s"Adding method entry request from pending $i")
          methodEntryManager.removeMethodEntryRequestWithId(i.requestId)
          methodEntryManager.createMethodEntryRequestFromInfo(i)
        })
      case methodExitManager: PendingMethodExitSupportLike =>
        methodExitManager.pendingMethodExitRequests.foreach(i => {
          _logger.trace(s"Adding method exit request from pending $i")
          methodExitManager.removeMethodExitRequestWithId(i.requestId)
          methodExitManager.createMethodExitRequestFromInfo(i)
        })
      case modificationWatchpointManager: PendingModificationWatchpointSupportLike =>
        modificationWatchpointManager.pendingModificationWatchpointRequests.foreach(i => {
          _logger.trace(s"Adding modification watchpoint request from pending $i")
          modificationWatchpointManager.removeModificationWatchpointRequestWithId(i.requestId)
          modificationWatchpointManager.createModificationWatchpointRequestFromInfo(i)
        })
      case monitorContendedEnteredManager: PendingMonitorContendedEnteredSupportLike =>
        monitorContendedEnteredManager.pendingMonitorContendedEnteredRequests.foreach(i => {
          _logger.trace(s"Adding monitor contended entered request from pending $i")
          monitorContendedEnteredManager.removeMonitorContendedEnteredRequest(i.requestId)
          monitorContendedEnteredManager.createMonitorContendedEnteredRequestFromInfo(i)
        })
      case monitorContendedEnterManager: PendingMonitorContendedEnterSupportLike =>
        monitorContendedEnterManager.pendingMonitorContendedEnterRequests.foreach(i => {
          _logger.trace(s"Adding monitor contended enter request from pending $i")
          monitorContendedEnterManager.removeMonitorContendedEnterRequest(i.requestId)
          monitorContendedEnterManager.createMonitorContendedEnterRequestFromInfo(i)
        })
      case monitorWaitedManager: PendingMonitorWaitedSupportLike =>
        monitorWaitedManager.pendingMonitorWaitedRequests.foreach(i => {
          _logger.trace(s"Adding monitor waited request from pending $i")
          monitorWaitedManager.removeMonitorWaitedRequest(i.requestId)
          monitorWaitedManager.createMonitorWaitedRequestFromInfo(i)
        })
      case monitorWaitManager: PendingMonitorWaitSupportLike =>
        monitorWaitManager.pendingMonitorWaitRequests.foreach(i => {
          _logger.trace(s"Adding monitor wait request from pending $i")
          monitorWaitManager.removeMonitorWaitRequest(i.requestId)
          monitorWaitManager.createMonitorWaitRequestFromInfo(i)
        })
      case stepManager: PendingStepSupportLike =>
        stepManager.pendingStepRequests.foreach(i => {
          _logger.trace(s"Adding step request from pending $i")
          stepManager.removeStepRequestWithId(i.requestId)
          stepManager.createStepRequestFromInfo(i)
        })
      case threadDeathManager: PendingThreadDeathSupportLike =>
        threadDeathManager.pendingThreadDeathRequests.foreach(i => {
          _logger.trace(s"Adding thread death request from pending $i")
          threadDeathManager.removeThreadDeathRequest(i.requestId)
          threadDeathManager.createThreadDeathRequestFromInfo(i)
        })
      case threadStartManager: PendingThreadStartSupportLike =>
        threadStartManager.pendingThreadStartRequests.foreach(i => {
          _logger.trace(s"Adding thread start request from pending $i")
          threadStartManager.removeThreadStartRequest(i.requestId)
          threadStartManager.createThreadStartRequestFromInfo(i)
        })
      case vmDeathManager: PendingVMDeathSupportLike =>
        vmDeathManager.pendingVMDeathRequests.foreach(i => {
          _logger.trace(s"Adding vm death request from pending $i")
          vmDeathManager.removeVMDeathRequest(i.requestId)
          vmDeathManager.createVMDeathRequestFromInfo(i)
        })
    }))
  }

  /**
   * Processes any pending requests in the managers of the provided manager
   * container by applying them to the managers in this manager container.
   *
   * @note This will not remove the pending requests from the managers
   *       contained in the provided manager container!
   * @param managerContainer The manager container whose managers with pending
   *                         requests will have their requests processed in
   *                         this manager container
   */
  def processPendingRequests(
    managerContainer: ManagerContainer
  ): Unit = synchronized {
    managerContainer.productIterator.foreach(m => Try(m match {
      case accessWatchpointManager: PendingAccessWatchpointSupportLike =>
        accessWatchpointManager.pendingAccessWatchpointRequests.foreach(i => {
          _logger.trace(s"Adding access watchpoint request from pending $i")
          this.accessWatchpointManager.createAccessWatchpointRequestFromInfo(i)
        })
      case breakpointManager: PendingBreakpointSupportLike =>
        breakpointManager.pendingBreakpointRequests.foreach(i => {
          _logger.trace(s"Adding breakpoint request from pending $i")
          this.breakpointManager.createBreakpointRequestFromInfo(i)
        })
      case classPrepareManager: PendingClassPrepareSupportLike =>
        classPrepareManager.pendingClassPrepareRequests.foreach(i => {
          _logger.trace(s"Adding class prepare request from pending $i")
          this.classPrepareManager.createClassPrepareRequestFromInfo(i)
        })
      case classUnloadManager: PendingClassUnloadSupportLike =>
        classUnloadManager.pendingClassUnloadRequests.foreach(i => {
          _logger.trace(s"Adding class unload request from pending $i")
          this.classUnloadManager.createClassUnloadRequestFromInfo(i)
        })
      case eventManager: PendingEventHandlerSupportLike =>
        eventManager.pendingEventHandlers.foreach(i => {
          _logger.trace(s"Adding event handler from pending $i")
          this.eventManager.addEventHandlerFromInfo(i)
        })
      case exceptionManager: PendingExceptionSupportLike =>
        exceptionManager.pendingExceptionRequests.foreach(i => {
          _logger.trace(s"Adding exception request from pending $i")
          this.exceptionManager.createExceptionRequestFromInfo(i)
        })
      case methodEntryManager: PendingMethodEntrySupportLike =>
        methodEntryManager.pendingMethodEntryRequests.foreach(i => {
          _logger.trace(s"Adding method entry request from pending $i")
          this.methodEntryManager.createMethodEntryRequestFromInfo(i)
        })
      case methodExitManager: PendingMethodExitSupportLike =>
        methodExitManager.pendingMethodExitRequests.foreach(i => {
          _logger.trace(s"Adding method exit request from pending $i")
          this.methodExitManager.createMethodExitRequestFromInfo(i)
        })
      case modificationWatchpointManager: PendingModificationWatchpointSupportLike =>
        modificationWatchpointManager.pendingModificationWatchpointRequests.foreach(i => {
          _logger.trace(s"Adding modification watchpoint request from pending $i")
          this.modificationWatchpointManager.createModificationWatchpointRequestFromInfo(i)
        })
      case monitorContendedEnteredManager: PendingMonitorContendedEnteredSupportLike =>
        monitorContendedEnteredManager.pendingMonitorContendedEnteredRequests.foreach(i => {
          _logger.trace(s"Adding monitor contended entered request from pending $i")
          this.monitorContendedEnteredManager.createMonitorContendedEnteredRequestFromInfo(i)
        })
      case monitorContendedEnterManager: PendingMonitorContendedEnterSupportLike =>
        monitorContendedEnterManager.pendingMonitorContendedEnterRequests.foreach(i => {
          _logger.trace(s"Adding monitor contended enter request from pending $i")
          this.monitorContendedEnterManager.createMonitorContendedEnterRequestFromInfo(i)
        })
      case monitorWaitedManager: PendingMonitorWaitedSupportLike =>
        monitorWaitedManager.pendingMonitorWaitedRequests.foreach(i => {
          _logger.trace(s"Adding monitor waited request from pending $i")
          this.monitorWaitedManager.createMonitorWaitedRequestFromInfo(i)
        })
      case monitorWaitManager: PendingMonitorWaitSupportLike =>
        monitorWaitManager.pendingMonitorWaitRequests.foreach(i => {
          _logger.trace(s"Adding monitor wait request from pending $i")
          this.monitorWaitManager.createMonitorWaitRequestFromInfo(i)
        })
      case stepManager: PendingStepSupportLike =>
        stepManager.pendingStepRequests.foreach(i => {
          _logger.trace(s"Adding step request from pending $i")
          this.stepManager.createStepRequestFromInfo(i)
        })
      case threadDeathManager: PendingThreadDeathSupportLike =>
        threadDeathManager.pendingThreadDeathRequests.foreach(i => {
          _logger.trace(s"Adding thread death request from pending $i")
          this.threadDeathManager.createThreadDeathRequestFromInfo(i)
        })
      case threadStartManager: PendingThreadStartSupportLike =>
        threadStartManager.pendingThreadStartRequests.foreach(i => {
          _logger.trace(s"Adding thread start request from pending $i")
          this.threadStartManager.createThreadStartRequestFromInfo(i)
        })
      case vmDeathManager: PendingVMDeathSupportLike =>
        vmDeathManager.pendingVMDeathRequests.foreach(i => {
          _logger.trace(s"Adding vm death request from pending $i")
          this.vmDeathManager.createVMDeathRequestFromInfo(i)
        })
    }))
  }

  /** Enables pending support for all managers supporting pending requests. */
  def enablePendingSupport(): Unit = setPendingSupportForAll(true)

  /** Disables pending support for all managers supporting pending requests. */
  def disablePendingSupport(): Unit = setPendingSupportForAll(false)

  /**
   * Sets the pending support enablement to the specified value.
   *
   * @param value True if enabling pending support on managers, otherwise false
   */
  private def setPendingSupportForAll(value: Boolean): Unit = {
    this.productIterator.foreach {
      case p: PendingRequestSupport => p.setPendingSupport(value)
      case _                        => // Do nothing in this case
    }
  }
}

/** Containers helper methods for initializing a manager container. */
object ManagerContainer {
  /**
   * Initializes all managers for the specified virtual machine. Uses the
   * default instance of the looping task runner for created managers.
   * Automatically starts the event manager.
   *
   * @param virtualMachine The virtual machine whose managers to initialize
   * @return The container holding all of the new managers
   */
  def fromVirtualMachine(virtualMachine: VirtualMachine): ManagerContainer = {
    val loopingTaskRunner = new LoopingTaskRunner()
    fromVirtualMachine(
      virtualMachine,
      loopingTaskRunner,
      autoStartEventManager = true
    )
  }

  /**
   * Initializes all managers for the specified virtual machine.
   *
   * @param virtualMachine The virtual machine whose managers to initialize
   * @param loopingTaskRunner The task runner to use with various managers
   * @return The container holding all of the new managers
   */
  def fromVirtualMachine(
    virtualMachine: VirtualMachine,
    loopingTaskRunner: LoopingTaskRunner,
    autoStartEventManager: Boolean
  ): ManagerContainer = {
    lazy val eventRequestManager = virtualMachine.eventRequestManager()
    lazy val eventQueue = virtualMachine.eventQueue()
    lazy val accessWatchpointManager =
      new StandardAccessWatchpointManager(eventRequestManager, classManager)
        with StandardPendingAccessWatchpointSupport
    lazy val breakpointManager =
      new StandardBreakpointManager(eventRequestManager, classManager)
        with StandardPendingBreakpointSupport
    lazy val classManager =
      new StandardClassManager(virtualMachine, loadClasses = true)
    lazy val classPrepareManager =
      new StandardClassPrepareManager(eventRequestManager)
        with StandardPendingClassPrepareSupport
    lazy val classUnloadManager =
      new StandardClassUnloadManager(eventRequestManager)
        with StandardPendingClassUnloadSupport
    lazy val eventManager = new StandardEventManager(
      eventQueue,
      loopingTaskRunner,
      autoStart = autoStartEventManager
    )
    lazy val exceptionManager =
      new StandardExceptionManager(virtualMachine, eventRequestManager)
        with StandardPendingExceptionSupport
    lazy val methodEntryManager =
      new StandardMethodEntryManager(eventRequestManager)
        with StandardPendingMethodEntrySupport
    lazy val methodExitManager =
      new StandardMethodExitManager(eventRequestManager)
        with StandardPendingMethodExitSupport
    lazy val modificationWatchpointManager =
      new StandardModificationWatchpointManager(eventRequestManager, classManager)
        with StandardPendingModificationWatchpointSupport
    lazy val monitorContendedEnteredManager =
      new StandardMonitorContendedEnteredManager(eventRequestManager)
        with StandardPendingMonitorContendedEnteredSupport
    lazy val monitorContendedEnterManager =
      new StandardMonitorContendedEnterManager(eventRequestManager)
        with StandardPendingMonitorContendedEnterSupport
    lazy val monitorWaitedManager =
      new StandardMonitorWaitedManager(eventRequestManager)
        with StandardPendingMonitorWaitedSupport
    lazy val monitorWaitManager =
      new StandardMonitorWaitManager(eventRequestManager)
        with StandardPendingMonitorWaitSupport
    lazy val requestManager =
      virtualMachine.eventRequestManager()
    lazy val stepManager =
      new StandardStepManager(eventRequestManager)
        with StandardPendingStepSupport
    lazy val threadDeathManager =
      new StandardThreadDeathManager(eventRequestManager)
        with StandardPendingThreadDeathSupport
    lazy val threadStartManager =
      new StandardThreadStartManager(eventRequestManager)
        with StandardPendingThreadStartSupport
    lazy val vmDeathManager =
      new StandardVMDeathManager(eventRequestManager)
        with StandardPendingVMDeathSupport

    ManagerContainer(
      accessWatchpointManager         = accessWatchpointManager,
      breakpointManager               = breakpointManager,
      classManager                    = classManager,
      classPrepareManager             = classPrepareManager,
      classUnloadManager              = classUnloadManager,
      eventManager                    = eventManager,
      exceptionManager                = exceptionManager,
      methodEntryManager              = methodEntryManager,
      methodExitManager               = methodExitManager,
      modificationWatchpointManager   = modificationWatchpointManager,
      monitorContendedEnteredManager  = monitorContendedEnteredManager,
      monitorContendedEnterManager    = monitorContendedEnterManager,
      monitorWaitedManager            = monitorWaitedManager,
      monitorWaitManager              = monitorWaitManager,
      requestManager                  = requestManager,
      stepManager                     = stepManager,
      threadDeathManager              = threadDeathManager,
      threadStartManager              = threadStartManager,
      vmDeathManager                  = vmDeathManager
    )
  }

  /**
   * Initializes all managers to dummy implementations with pending requests
   * enabled to allow setting requests ahead of virtual machine connections.
   *
   * @note Currently, classManager and requestManager are null!
   * @return The container holding all of the new managers
   */
  def usingDummyManagers(): ManagerContainer = {
    lazy val accessWatchpointManager =
      new DummyAccessWatchpointManager
        with StandardPendingAccessWatchpointSupport
    lazy val breakpointManager =
      new DummyBreakpointManager
        with StandardPendingBreakpointSupport
    lazy val classManager = null
    lazy val classPrepareManager =
      new DummyClassPrepareManager
        with StandardPendingClassPrepareSupport
    lazy val classUnloadManager =
      new DummyClassUnloadManager
        with StandardPendingClassUnloadSupport
    lazy val eventManager =
      new DummyEventManager
        with StandardPendingEventHandlerSupport
    lazy val exceptionManager =
      new DummyExceptionManager
        with StandardPendingExceptionSupport
    lazy val methodEntryManager =
      new DummyMethodEntryManager
        with StandardPendingMethodEntrySupport
    lazy val methodExitManager =
      new DummyMethodExitManager
        with StandardPendingMethodExitSupport
    lazy val modificationWatchpointManager =
      new DummyModificationWatchpointManager
        with StandardPendingModificationWatchpointSupport
    lazy val monitorContendedEnteredManager =
      new DummyMonitorContendedEnteredManager
        with StandardPendingMonitorContendedEnteredSupport
    lazy val monitorContendedEnterManager =
      new DummyMonitorContendedEnterManager
        with StandardPendingMonitorContendedEnterSupport
    lazy val monitorWaitedManager =
      new DummyMonitorWaitedManager
        with StandardPendingMonitorWaitedSupport
    lazy val monitorWaitManager =
      new DummyMonitorWaitManager
        with StandardPendingMonitorWaitSupport
    lazy val requestManager = null
    lazy val stepManager =
      new DummyStepManager
        with StandardPendingStepSupport
    lazy val threadDeathManager =
      new DummyThreadDeathManager
        with StandardPendingThreadDeathSupport
    lazy val threadStartManager =
      new DummyThreadStartManager
        with StandardPendingThreadStartSupport
    lazy val vmDeathManager =
      new DummyVMDeathManager
        with StandardPendingVMDeathSupport

    ManagerContainer(
      accessWatchpointManager         = accessWatchpointManager,
      breakpointManager               = breakpointManager,
      classManager                    = classManager,
      classPrepareManager             = classPrepareManager,
      classUnloadManager              = classUnloadManager,
      eventManager                    = eventManager,
      exceptionManager                = exceptionManager,
      methodEntryManager              = methodEntryManager,
      methodExitManager               = methodExitManager,
      modificationWatchpointManager   = modificationWatchpointManager,
      monitorContendedEnteredManager  = monitorContendedEnteredManager,
      monitorContendedEnterManager    = monitorContendedEnterManager,
      monitorWaitedManager            = monitorWaitedManager,
      monitorWaitManager              = monitorWaitManager,
      requestManager                  = requestManager,
      stepManager                     = stepManager,
      threadDeathManager              = threadDeathManager,
      threadStartManager              = threadStartManager,
      vmDeathManager                  = vmDeathManager
    )
  }
}
