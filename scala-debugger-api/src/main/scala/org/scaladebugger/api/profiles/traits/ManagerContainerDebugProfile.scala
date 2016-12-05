package org.scaladebugger.api.profiles.traits

import org.scaladebugger.api.lowlevel.ManagerContainer

/**
 * Represents a debug profile that uses managers provided by a manager container
 * as the underlying implementation.
 */
trait ManagerContainerDebugProfile extends DebugProfile {
  // The container of low-level managers to use as the underlying implementation
  protected val managerContainer: ManagerContainer

  protected lazy val accessWatchpointManager =
    managerContainer.accessWatchpointManager

  protected lazy val breakpointManager = managerContainer.breakpointManager

  protected lazy val classManager = managerContainer.classManager

  protected lazy val classPrepareManager = managerContainer.classPrepareManager

  protected lazy val classUnloadManager = managerContainer.classUnloadManager

  protected lazy val eventManager = managerContainer.eventManager

  protected lazy val exceptionManager = managerContainer.exceptionManager

  protected lazy val modificationWatchpointManager =
    managerContainer.modificationWatchpointManager

  protected lazy val monitorContendedEnteredManager =
    managerContainer.monitorContendedEnteredManager

  protected lazy val monitorContendedEnterManager =
    managerContainer.monitorContendedEnterManager

  protected lazy val monitorWaitedManager =
    managerContainer.monitorWaitedManager

  protected lazy val monitorWaitManager =
    managerContainer.monitorWaitManager

  protected lazy val methodEntryManager = managerContainer.methodEntryManager

  protected lazy val methodExitManager = managerContainer.methodExitManager

  protected lazy val stepManager = managerContainer.stepManager

  protected lazy val threadDeathManager = managerContainer.threadDeathManager

  protected lazy val threadStartManager = managerContainer.threadStartManager

  protected lazy val vmDeathManager = managerContainer.vmDeathManager
}
