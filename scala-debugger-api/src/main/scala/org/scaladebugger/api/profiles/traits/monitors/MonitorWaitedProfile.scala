package org.scaladebugger.api.profiles.traits.monitors
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.monitors.MonitorWaitedRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorWaitedEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * monitor waited functionality for a specific debug profile.
 */
trait MonitorWaitedProfile {
  /** Represents a monitor waited event and any associated data. */
  type MonitorWaitedEventAndData =
    (MonitorWaitedEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending monitor waited requests.
   *
   * @return The collection of information on monitor waited requests
   */
  def monitorWaitedRequests: Seq[MonitorWaitedRequestInfo]

  /**
   * Constructs a stream of monitor waited events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor waited events
   */
  def tryGetOrCreateMonitorWaitedRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitedEventInfoProfile]] = {
    tryGetOrCreateMonitorWaitedRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of monitor waited events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor waited events and any retrieved
   *         data based on requests from extra arguments
   */
  def tryGetOrCreateMonitorWaitedRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitedEventAndData]]

  /**
   * Constructs a stream of monitor waited events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor waited events
   */
  def getOrCreateMonitorWaitedRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitedEventInfoProfile] = {
    tryGetOrCreateMonitorWaitedRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of monitor waited events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of monitor waited events and any retrieved
   *         data based on requests from extra arguments
   */
  def getOrCreateMonitorWaitedRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitedEventAndData] = {
    tryGetOrCreateMonitorWaitedRequestWithData(extraArguments: _*).get
  }

  /**
   * Determines if the monitor waited request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       monitor waited request
   * @return True if there is at least one monitor waited request
   *         with the provided extra arguments that is pending, otherwise false
   */
  def isMonitorWaitedRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all monitor waited requests with the specified
   * extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor waited request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeMonitorWaitedRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[MonitorWaitedRequestInfo]

  /**
   * Removes all monitor waited requests with the specified extra
   * arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       monitor waited request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveMonitorWaitedRequestWithArgs(
    extraArguments: JDIArgument*
  ): Try[Option[MonitorWaitedRequestInfo]] = Try(removeMonitorWaitedRequestWithArgs(
    extraArguments: _*
  ))

  /**
   * Removes all monitor waited requests.
   *
   * @return The collection of information about removed
   *         monitor waited requests
   */
  def removeAllMonitorWaitedRequests(): Seq[MonitorWaitedRequestInfo]

  /**
   * Removes all monitor waited requests.
   *
   * @return Success containing the collection of information about removed
   *         monitor waited requests, otherwise a failure
   */
  def tryRemoveAllMonitorWaitedRequests(): Try[Seq[MonitorWaitedRequestInfo]] = Try(
    removeAllMonitorWaitedRequests()
  )
}
