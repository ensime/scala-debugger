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
  class TestEventInfo(
    val scalaVirtualMachine: ScalaVirtualMachine,
    val isJavaInfo: Boolean
  ) extends EventInfo {
    override def toJavaInfo: EventInfo = throwException()
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
    override def toAccessWatchpointEvent: AccessWatchpointEventInfo = throwException()
    override def toBreakpointEvent: BreakpointEventInfo = throwException()
    override def toClassPrepareEvent: ClassPrepareEventInfo = throwException()
    override def toClassUnloadEvent: ClassUnloadEventInfo = throwException()
    override def toExceptionEvent: ExceptionEventInfo = throwException()
    override def toLocatableEvent: LocatableEventInfo = throwException()
    override def toMethodEntryEvent: MethodEntryEventInfo = throwException()
    override def toMethodExitEvent: MethodExitEventInfo = throwException()
    override def toModificationWatchpointEvent: ModificationWatchpointEventInfo = throwException()
    override def toMonitorEvent: MonitorEventInfo = throwException()
    override def toMonitorContendedEnteredEvent: MonitorContendedEnteredEventInfo = throwException()
    override def toMonitorContendedEnterEvent: MonitorContendedEnterEventInfo = throwException()
    override def toMonitorWaitedEvent: MonitorWaitedEventInfo = throwException()
    override def toMonitorWaitEvent: MonitorWaitEventInfo = throwException()
    override def toStepEvent: StepEventInfo = throwException()
    override def toThreadDeathEvent: ThreadDeathEventInfo = throwException()
    override def toThreadStartEvent: ThreadStartEventInfo = throwException()
    override def toVMDeathEvent: VMDeathEventInfo = throwException()
    override def toVMDisconnectEvent: VMDisconnectEventInfo = throwException()
    override def toVMStartEvent: VMStartEventInfo = throwException()
    override def toWatchpointEvent: WatchpointEventInfo = throwException()
  }
}
