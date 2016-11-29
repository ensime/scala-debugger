package org.scaladebugger.api.profiles.traits.requests.monitors
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.monitors.MonitorWaitRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorWaitEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * monitor wait functionality for a specific debug profile.
 */
trait MonitorWaitProfile {
  /** Represents a monitor wait event and any associated data. */
  type MonitorWaitEventAndData =
    (MonitorWaitEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending monitor wait requests.
   *
   * @return The collection of information on monitor wait requests
   */
  def monitorWaitRequests: Seq[MonitorWaitRequestInfo]

  /**
   * Constructs a stream of monitor wait events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor wait events
   */
  def tryGetOrCreateMonitorWaitRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitEventInfoProfile]] = {
    tryGetOrCreateMonitorWaitRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of monitor wait events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor wait events and any retrieved
   *         data based on requests from extra arguments
   */
  def tryGetOrCreateMonitorWaitRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitEventAndData]]

  /**
   * Constructs a stream of monitor wait events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor wait events
   */
  def getOrCreateMonitorWaitRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitEventInfoProfile] = {
    tryGetOrCreateMonitorWaitRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of monitor wait events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor wait events and any retrieved
   *         data based on requests from extra arguments
   */
  def getOrCreateMonitorWaitRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitEventAndData] = {
    tryGetOrCreateMonitorWaitRequestWithData(extraArguments: _*).get
  }

  /**
   * Determines if the monitor wait request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       monitor wait request
   * @return True if there is at least one monitor wait request
   *         with the provided extra arguments that is pending, otherwise false
   */
  def isMonitorWaitRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all monitor wait requests with the specified
   * extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor wait request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeMonitorWaitRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[MonitorWaitRequestInfo]

  /**
   * Removes all monitor wait requests with the specified extra
   * arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor wait request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveMonitorWaitRequestWithArgs(
    extraArguments: JDIArgument*
  ): Try[Option[MonitorWaitRequestInfo]] = Try(removeMonitorWaitRequestWithArgs(
    extraArguments: _*
  ))

  /**
   * Removes all monitor wait requests.
   *
   * @return The collection of information about removed
   *         monitor wait requests
   */
  def removeAllMonitorWaitRequests(): Seq[MonitorWaitRequestInfo]

  /**
   * Removes all monitor wait requests.
   *
   * @return Success containing the collection of information about removed
   *         monitor wait requests, otherwise a failure
   */
  def tryRemoveAllMonitorWaitRequests(): Try[Seq[MonitorWaitRequestInfo]] = Try(
    removeAllMonitorWaitRequests()
  )
}
