package test

import com.sun.jdi.request.EventRequestManager
import org.scaladebugger.api.lowlevel.PendingRequestSupport
import org.scaladebugger.api.lowlevel.breakpoints.BreakpointManager
import org.scaladebugger.api.lowlevel.classes.{ClassUnloadManager, ClassPrepareManager, ClassManager}
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.exceptions.ExceptionManager
import org.scaladebugger.api.lowlevel.methods.{MethodExitManager, MethodEntryManager}
import org.scaladebugger.api.lowlevel.monitors.{MonitorWaitManager, MonitorWaitedManager, MonitorContendedEnterManager, MonitorContendedEnteredManager}
import org.scaladebugger.api.lowlevel.steps.StepManager
import org.scaladebugger.api.lowlevel.threads.{ThreadStartManager, ThreadDeathManager}
import org.scaladebugger.api.lowlevel.vm.VMDeathManager
import org.scaladebugger.api.lowlevel.watchpoints.{ModificationWatchpointManager, AccessWatchpointManager}

/**
 * Contains all managers with pending support enabled.
 */
object PendingManagers {
  //
  // NOTE: The following classes are explicitly created due to a limitation
  //       of ScalaMock where you cannot issue
  //
  //           mock[VMDeathManager with PendingRequestSupport]
  //
  trait TestPendingAccessWatchpointManager extends AccessWatchpointManager with PendingRequestSupport
  trait TestPendingBreakpointManager extends BreakpointManager with PendingRequestSupport
  trait TestPendingClassManager extends ClassManager with PendingRequestSupport
  trait TestPendingClassPrepareManager extends ClassPrepareManager with PendingRequestSupport
  trait TestPendingClassUnloadManager extends ClassUnloadManager with PendingRequestSupport
  trait TestPendingEventManager extends EventManager with PendingRequestSupport
  trait TestPendingExceptionManager extends ExceptionManager with PendingRequestSupport
  trait TestPendingMethodEntryManager extends MethodEntryManager with PendingRequestSupport
  trait TestPendingMethodExitManager extends MethodExitManager with PendingRequestSupport
  trait TestPendingModificationWatchpointManager extends ModificationWatchpointManager with PendingRequestSupport
  trait TestPendingMonitorContendedEnteredManager extends MonitorContendedEnteredManager with PendingRequestSupport
  trait TestPendingMonitorContendedEnterManager extends MonitorContendedEnterManager with PendingRequestSupport
  trait TestPendingMonitorWaitedManager extends MonitorWaitedManager with PendingRequestSupport
  trait TestPendingMonitorWaitManager extends MonitorWaitManager with PendingRequestSupport
  trait TestPendingEventRequestManager extends EventRequestManager with PendingRequestSupport
  trait TestPendingStepManager extends StepManager with PendingRequestSupport
  trait TestPendingThreadDeathManager extends ThreadDeathManager with PendingRequestSupport
  trait TestPendingThreadStartManager extends ThreadStartManager with PendingRequestSupport
  trait TestPendingVMDeathManager extends VMDeathManager with PendingRequestSupport
}
