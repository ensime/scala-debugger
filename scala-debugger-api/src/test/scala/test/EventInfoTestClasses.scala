package test

import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.profiles.traits.info.events._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Contains test implementations of info classes. All methods throw an error.
 */
object EventInfoTestClasses {
  class NotOverriddenException extends Exception

  /** Exception thrown by all methods. */
  val DefaultException = new NotOverriddenException
  private def throwException() = throw DefaultException

  /**
   * Represents a stubbed event info profile where we can override specific
   * methods without worrying about overriding all of them each time.
   *
   * All isNNNEvent yield false by default. Everything else throws an exception.
   */
  class TestEventInfoProfile(
    val scalaVirtualMachine: ScalaVirtualMachine,
    val isJavaInfo: Boolean
  ) extends EventInfoProfile {
    override def toJavaInfo: EventInfoProfile = throwException()
    override def toJdiInstance: Event = throwException()
    override def requestArguments: Seq[JDIRequestArgument] = throwException()
    override def eventArguments: Seq[JDIEventArgument] = throwException()
    override def isAccessWatchpointEvent: Boolean = false
    override def isBreakpointEvent: Boolean = false
    override def isClassPrepareEvent: Boolean = false
    override def isClassUnloadEvent: Boolean = false
    override def isExceptionEvent: Boolean = false
    override def isLocatableEvent: Boolean = false
    override def isMethodEntryEvent: Boolean = false
    override def isMethodExitEvent: Boolean = false
    override def isModificationWatchpointEvent: Boolean = false
    override def isMonitorContendedEnteredEvent: Boolean = false
    override def isMonitorContendedEnterEvent: Boolean = false
    override def isMonitorWaitedEvent: Boolean = false
    override def isMonitorWaitEvent: Boolean = false
    override def isStepEvent: Boolean = false
    override def isThreadDeathEvent: Boolean = false
    override def isThreadStartEvent: Boolean = false
    override def isVMDeathEvent: Boolean = false
    override def isVMDisconnectEvent: Boolean = false
    override def isVMStartEvent: Boolean = false
    override def isWatchpointEvent: Boolean = false
    override def isPlainEvent: Boolean = false
    override def toAccessWatchpointEvent: AccessWatchpointEventInfoProfile = throwException()
    override def toBreakpointEvent: BreakpointEventInfoProfile = throwException()
    override def toClassPrepareEvent: ClassPrepareEventInfoProfile = throwException()
    override def toClassUnloadEvent: ClassUnloadEventInfoProfile = throwException()
    override def toExceptionEvent: ExceptionEventInfoProfile = throwException()
    override def toLocatableEvent: LocatableEventInfoProfile = throwException()
    override def toMethodEntryEvent: MethodEntryEventInfoProfile = throwException()
    override def toMethodExitEvent: MethodExitEventInfoProfile = throwException()
    override def toModificationWatchpointEvent: ModificationWatchpointEventInfoProfile = throwException()
    override def toMonitorEvent: MonitorEventInfoProfile = throwException()
    override def toMonitorContendedEnteredEvent: MonitorContendedEnteredEventInfoProfile = throwException()
    override def toMonitorContendedEnterEvent: MonitorContendedEnterEventInfoProfile = throwException()
    override def toMonitorWaitedEvent: MonitorWaitedEventInfoProfile = throwException()
    override def toMonitorWaitEvent: MonitorWaitEventInfoProfile = throwException()
    override def toStepEvent: StepEventInfoProfile = throwException()
    override def toThreadDeathEvent: ThreadDeathEventInfoProfile = throwException()
    override def toThreadStartEvent: ThreadStartEventInfoProfile = throwException()
    override def toVMDeathEvent: VMDeathEventInfoProfile = throwException()
    override def toVMDisconnectEvent: VMDisconnectEventInfoProfile = throwException()
    override def toVMStartEvent: VMStartEventInfoProfile = throwException()
    override def toWatchpointEvent: WatchpointEventInfoProfile = throwException()
  }
}
