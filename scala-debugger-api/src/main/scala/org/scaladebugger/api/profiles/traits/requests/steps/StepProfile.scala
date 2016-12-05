package org.scaladebugger.api.profiles.traits.requests.steps

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.steps.StepRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.ThreadInfoProfile
import org.scaladebugger.api.profiles.traits.info.events.StepEventInfoProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * step functionality for a specific debug profile.
 */
trait StepProfile {
  /** Represents a step event and any associated data. */
  type StepEventAndData = (StepEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending step requests.
   *
   * @return The collection of information on step requests
   */
  def stepRequests: Seq[StepRequestInfo]

  /**
   * Steps in from the current location to the next line.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event
   */
  def stepIntoLine(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventInfoProfile] = {
    stepIntoLineWithData(threadInfoProfile, extraArguments: _*).map(_._1)
  }

  /**
   * Steps in from the current location to the next line.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepIntoLineWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Steps over from the current location to the next line.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event
   */
  def stepOverLine(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventInfoProfile] = {
    stepOverLineWithData(threadInfoProfile, extraArguments: _*).map(_._1)
  }

  /**
   * Steps over from the current location to the next line.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOverLineWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Steps out from the current location to the next line.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event
   */
  def stepOutLine(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventInfoProfile] = {
    stepOutLineWithData(threadInfoProfile, extraArguments: _*).map(_._1)
  }

  /**
   * Steps out from the current location to the next line.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOutLineWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Steps in from the current location to the next location.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event
   */
  def stepIntoMin(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventInfoProfile] = {
    stepIntoMinWithData(threadInfoProfile, extraArguments: _*).map(_._1)
  }

  /**
   * Steps in from the current location to the next location.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepIntoMinWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Steps over from the current location to the next location.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event
   */
  def stepOverMin(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventInfoProfile] = {
    stepOverMinWithData(threadInfoProfile, extraArguments: _*).map(_._1)
  }

  /**
   * Steps over from the current location to the next location.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOverMinWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Steps out from the current location to the next location.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event
   */
  def stepOutMin(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventInfoProfile] = {
    stepOutMinWithData(threadInfoProfile, extraArguments: _*).map(_._1)
  }

  /**
   * Steps out from the current location to the next location.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOutMinWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Constructs a stream of step events.
   *
   * @param threadInfoProfile The thread with which to receive step events
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of step events
   */
  def tryCreateStepListener(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepEventInfoProfile]] = {
    tryCreateStepListenerWithData(threadInfoProfile, extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of step events.
   *
   * @param threadInfoProfile The thread with which to receive step events
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of step events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryCreateStepListenerWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepEventAndData]]

  /**
   * Constructs a stream of step events.
   *
   * @param threadInfoProfile The thread with which to receive step events
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of step events
   */
  def createStepListener(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): IdentityPipeline[StepEventInfoProfile] = {
    tryCreateStepListener(threadInfoProfile, extraArguments: _*).get
  }

  /**
   * Constructs a stream of step events.
   *
   * @param threadInfoProfile The thread with which to receive step events
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of step events and any retrieved data based on
   *         requests from extra arguments
   */
  def createStepListenerWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): IdentityPipeline[StepEventAndData] = {
    tryCreateStepListenerWithData(threadInfoProfile, extraArguments: _*).get
  }

  /**
   * Determines if there is any step request for the specified thread that
   * is pending.
   *
   * @param threadInfoProfile The thread with which is receiving the step request
   * @return True if there is at least one step request with the
   *         specified name in the specified class that is pending,
   *         otherwise false
   */
  def isStepRequestPending(threadInfoProfile: ThreadInfoProfile): Boolean

  /**
   * Determines if there is any step request for the specified thread with
   * matching arguments that is pending.
   *
   * @param threadInfoProfile The thread with which is receiving the step request
   * @param extraArguments The additional arguments provided to the specific
   *                       step request
   * @return True if there is at least one step request with the
   *         specified name and arguments in the specified class that is
   *         pending, otherwise false
   */
  def isStepRequestWithArgsPending(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all step requests for the given thread.
   *
   * @param threadInfoProfile The thread with which is receiving the step request
   * @return The collection of information about removed step requests
   */
  def removeStepRequests(
    threadInfoProfile: ThreadInfoProfile
  ): Seq[StepRequestInfo]

  /**
   * Removes all step requests for the given thread.
   *
   * @param threadInfoProfile The thread with which is receiving the step request
   * @return Success containing the collection of information about removed
   *         step requests, otherwise a failure
   */
  def tryRemoveStepRequests(
    threadInfoProfile: ThreadInfoProfile
  ): Try[Seq[StepRequestInfo]] = Try(removeStepRequests(
    threadInfoProfile
  ))

  /**
   * Removes all step requests for the given thread with the specified extra
   * arguments.
   *
   * @param threadInfoProfile The thread with which is receiving the step request
   * @param extraArguments the additional arguments provided to the specific
   *                       step request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeStepRequestWithArgs(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Option[StepRequestInfo]

  /**
   * Removes all step requests for the given thread with the specified extra
   * arguments.
   *
   * @param threadInfoProfile The thread with which is receiving the step request
   * @param extraArguments the additional arguments provided to the specific
   *                       step request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveStepRequestWithArgs(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Try[Option[StepRequestInfo]] = Try(removeStepRequestWithArgs(
    threadInfoProfile,
    extraArguments: _*
  ))

  /**
   * Removes all step requests.
   *
   * @return The collection of information about removed step requests
   */
  def removeAllStepRequests(): Seq[StepRequestInfo]

  /**
   * Removes all step requests.
   *
   * @return Success containing the collection of information about removed
   *         step requests, otherwise a failure
   */
  def tryRemoveAllStepRequests(): Try[Seq[StepRequestInfo]] = Try(
    removeAllStepRequests()
  )
}
