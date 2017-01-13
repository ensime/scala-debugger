package org.scaladebugger.api.profiles.traits.requests.steps

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.steps.StepRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.profiles.traits.info.events.StepEventInfo

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * step functionality for a specific debug profile.
 */
trait StepRequest {
  /** Represents a step event and any associated data. */
  type StepEventAndData = (StepEventInfo, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending step requests.
   *
   * @return The collection of information on step requests
   */
  def stepRequests: Seq[StepRequestInfo]

  /**
   * Steps in from the current location to the next line.
   *
   * @param threadInfo The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event
   */
  def stepIntoLine(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventInfo] = {
    stepIntoLineWithData(threadInfo, extraArguments: _*).map(_._1)
  }

  /**
   * Steps in from the current location to the next line.
   *
   * @param threadInfo The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepIntoLineWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Steps over from the current location to the next line.
   *
   * @param threadInfo The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event
   */
  def stepOverLine(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventInfo] = {
    stepOverLineWithData(threadInfo, extraArguments: _*).map(_._1)
  }

  /**
   * Steps over from the current location to the next line.
   *
   * @param threadInfo The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOverLineWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Steps out from the current location to the next line.
   *
   * @param threadInfo The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event
   */
  def stepOutLine(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventInfo] = {
    stepOutLineWithData(threadInfo, extraArguments: _*).map(_._1)
  }

  /**
   * Steps out from the current location to the next line.
   *
   * @param threadInfo The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOutLineWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Steps in from the current location to the next location.
   *
   * @param threadInfo The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event
   */
  def stepIntoMin(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventInfo] = {
    stepIntoMinWithData(threadInfo, extraArguments: _*).map(_._1)
  }

  /**
   * Steps in from the current location to the next location.
   *
   * @param threadInfo The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepIntoMinWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Steps over from the current location to the next location.
   *
   * @param threadInfo The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event
   */
  def stepOverMin(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventInfo] = {
    stepOverMinWithData(threadInfo, extraArguments: _*).map(_._1)
  }

  /**
   * Steps over from the current location to the next location.
   *
   * @param threadInfo The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOverMinWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Steps out from the current location to the next location.
   *
   * @param threadInfo The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event
   */
  def stepOutMin(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventInfo] = {
    stepOutMinWithData(threadInfo, extraArguments: _*).map(_._1)
  }

  /**
   * Steps out from the current location to the next location.
   *
   * @param threadInfo The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting one-time event and any retrieved data based on
   *         requests from extra arguments
   */
  def stepOutMinWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData]

  /**
   * Constructs a stream of step events.
   *
   * @param threadInfo The thread with which to receive step events
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of step events
   */
  def tryCreateStepListener(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepEventInfo]] = tryCreateStepListenerWithData(
    threadInfo,
    extraArguments: _*
  ).map(_.map(_._1).noop())

  /**
   * Constructs a stream of step events.
   *
   * @param threadInfo The thread with which to receive step events
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of step events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryCreateStepListenerWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepEventAndData]]

  /**
   * Constructs a stream of step events.
   *
   * @param threadInfo The thread with which to receive step events
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of step events
   */
  def createStepListener(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): IdentityPipeline[StepEventInfo] = {
    tryCreateStepListener(threadInfo, extraArguments: _*).get
  }

  /**
   * Constructs a stream of step events.
   *
   * @param threadInfo The thread with which to receive step events
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of step events and any retrieved data based on
   *         requests from extra arguments
   */
  def createStepListenerWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): IdentityPipeline[StepEventAndData] = {
    tryCreateStepListenerWithData(threadInfo, extraArguments: _*).get
  }

  /**
   * Determines if there is any step request for the specified thread that
   * is pending.
   *
   * @param threadInfo The thread with which is receiving the step request
   * @return True if there is at least one step request with the
   *         specified name in the specified class that is pending,
   *         otherwise false
   */
  def isStepRequestPending(threadInfo: ThreadInfo): Boolean

  /**
   * Determines if there is any step request for the specified thread with
   * matching arguments that is pending.
   *
   * @param threadInfo The thread with which is receiving the step request
   * @param extraArguments The additional arguments provided to the specific
   *                       step request
   * @return True if there is at least one step request with the
   *         specified name and arguments in the specified class that is
   *         pending, otherwise false
   */
  def isStepRequestWithArgsPending(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all step requests for the given thread.
   *
   * @param threadInfo The thread with which is receiving the step request
   * @return The collection of information about removed step requests
   */
  def removeStepRequests(
    threadInfo: ThreadInfo
  ): Seq[StepRequestInfo]

  /**
   * Removes all step requests for the given thread.
   *
   * @param threadInfo The thread with which is receiving the step request
   * @return Success containing the collection of information about removed
   *         step requests, otherwise a failure
   */
  def tryRemoveStepRequests(
    threadInfo: ThreadInfo
  ): Try[Seq[StepRequestInfo]] = Try(removeStepRequests(
    threadInfo
  ))

  /**
   * Removes all step requests for the given thread with the specified extra
   * arguments.
   *
   * @param threadInfo The thread with which is receiving the step request
   * @param extraArguments the additional arguments provided to the specific
   *                       step request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeStepRequestWithArgs(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Option[StepRequestInfo]

  /**
   * Removes all step requests for the given thread with the specified extra
   * arguments.
   *
   * @param threadInfo The thread with which is receiving the step request
   * @param extraArguments the additional arguments provided to the specific
   *                       step request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveStepRequestWithArgs(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Try[Option[StepRequestInfo]] = Try(removeStepRequestWithArgs(
    threadInfo,
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
