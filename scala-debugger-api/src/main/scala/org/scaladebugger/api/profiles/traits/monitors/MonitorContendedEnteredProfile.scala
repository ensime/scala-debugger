package org.scaladebugger.api.profiles.traits.monitors
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.monitors.MonitorContendedEnteredRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorContendedEnteredEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * monitor contended entered functionality for a specific debug profile.
 */
trait MonitorContendedEnteredProfile {
  /** Represents a monitor contended entered event and any associated data. */
  type MonitorContendedEnteredEventAndData =
    (MonitorContendedEnteredEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending monitor contended entered
   * requests.
   *
   * @return The collection of information on monitor contended entered requests
   */
  def monitorContendedEnteredRequests: Seq[MonitorContendedEnteredRequestInfo]

  /**
   * Constructs a stream of monitor contended entered events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended entered events
   */
  def tryGetOrCreateMonitorContendedEnteredRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnteredEventInfoProfile]] = {
    tryGetOrCreateMonitorContendedEnteredRequestWithData(extraArguments: _*)
      .map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of monitor contended entered events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended entered events and any retrieved
   *         data based on requests from extra arguments
   */
  def tryGetOrCreateMonitorContendedEnteredRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnteredEventAndData]]

  /**
   * Constructs a stream of monitor contended entered events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended entered events
   */
  def getOrCreateMonitorContendedEnteredRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnteredEventInfoProfile] = {
    tryGetOrCreateMonitorContendedEnteredRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of monitor contended entered events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor contended entered events and any retrieved
   *         data based on requests from extra arguments
   */
  def getOrCreateMonitorContendedEnteredRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnteredEventAndData] = {
    tryGetOrCreateMonitorContendedEnteredRequestWithData(extraArguments: _*).get
  }

  /**
   * Determines if the monitor contended entered request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       monitor contended entered request
   * @return True if there is at least one monitor contended entered request
   *         with the provided extra arguments that is pending, otherwise false
   */
  def isMonitorContendedEnteredRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all monitor contended entered requests with the specified
   * extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor contended entered request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeMonitorContendedEnteredRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[MonitorContendedEnteredRequestInfo]

  /**
   * Removes all monitor contended entered requests with the specified extra
   * arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor contended entered request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveMonitorContendedEnteredRequestWithArgs(
    extraArguments: JDIArgument*
  ): Try[Option[MonitorContendedEnteredRequestInfo]] = Try(removeMonitorContendedEnteredRequestWithArgs(
    extraArguments: _*
  ))

  /**
   * Removes all monitor contended entered requests.
   *
   * @return The collection of information about removed
   *         monitor contended entered requests
   */
  def removeAllMonitorContendedEnteredRequests(): Seq[MonitorContendedEnteredRequestInfo]

  /**
   * Removes all monitor contended entered requests.
   *
   * @return Success containing the collection of information about removed
   *         monitor contended entered requests, otherwise a failure
   */
  def tryRemoveAllMonitorContendedEnteredRequests(): Try[Seq[MonitorContendedEnteredRequestInfo]] = Try(
    removeAllMonitorContendedEnteredRequests()
  )
}
