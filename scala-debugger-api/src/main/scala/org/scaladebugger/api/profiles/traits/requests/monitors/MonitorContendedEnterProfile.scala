package org.scaladebugger.api.profiles.traits.requests.monitors

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.monitors.MonitorContendedEnterRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorContendedEnterEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * monitor contended enter functionality for a specific debug profile.
 */
trait MonitorContendedEnterProfile {
  /** Represents a monitor contended enter event and any associated data. */
  type MonitorContendedEnterEventAndData =
    (MonitorContendedEnterEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending monitor contended enter
   * requests.
   *
   * @return The collection of information on monitor contended enter requests
   */
  def monitorContendedEnterRequests: Seq[MonitorContendedEnterRequestInfo]

  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended enter events
   */
  def tryGetOrCreateMonitorContendedEnterRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnterEventInfoProfile]] = {
    tryGetOrCreateMonitorContendedEnterRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended enter events and any retrieved
   *         data based on requests from extra arguments
   */
  def tryGetOrCreateMonitorContendedEnterRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnterEventAndData]]

  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended enter events
   */
  def getOrCreateMonitorContendedEnterRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnterEventInfoProfile] = {
    tryGetOrCreateMonitorContendedEnterRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of monitor contended enter events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended enter events and any retrieved
   *         data based on requests from extra arguments
   */
  def getOrCreateMonitorContendedEnterRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnterEventAndData] = {
    tryGetOrCreateMonitorContendedEnterRequestWithData(extraArguments: _*).get
  }

  /**
   * Determines if the monitor contended enter request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       monitor contended enter request
   * @return True if there is at least one monitor contended enter request
   *         with the provided extra arguments that is pending, otherwise false
   */
  def isMonitorContendedEnterRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all monitor contended enter requests with the specified
   * extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor contended enter request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeMonitorContendedEnterRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[MonitorContendedEnterRequestInfo]

  /**
   * Removes all monitor contended enter requests with the specified extra
   * arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor contended enter request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveMonitorContendedEnterRequestWithArgs(
    extraArguments: JDIArgument*
  ): Try[Option[MonitorContendedEnterRequestInfo]] = Try(removeMonitorContendedEnterRequestWithArgs(
    extraArguments: _*
  ))

  /**
   * Removes all monitor contended enter requests.
   *
   * @return The collection of information about removed
   *         monitor contended enter requests
   */
  def removeAllMonitorContendedEnterRequests(): Seq[MonitorContendedEnterRequestInfo]

  /**
   * Removes all monitor contended enter requests.
   *
   * @return Success containing the collection of information about removed
   *         monitor contended enter requests, otherwise a failure
   */
  def tryRemoveAllMonitorContendedEnterRequests(): Try[Seq[MonitorContendedEnterRequestInfo]] = Try(
    removeAllMonitorContendedEnterRequests()
  )
}
