package org.scaladebugger.api.profiles.pure.steps
//import acyclic.file

import com.sun.jdi.ThreadReference
import com.sun.jdi.event.StepEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventType.StepEventType
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.filters.ThreadFilter
import org.scaladebugger.api.lowlevel.steps._
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, ThreadInfoProfile}
import org.scaladebugger.api.profiles.traits.steps.StepProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * Represents a pure profile for steps that adds no
 * extra logic on top of the standard JDI.
 */
trait PureStepProfile extends StepProfile {
  protected val stepManager: StepManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducerProfile

  private lazy val eventProducer = infoProducer.eventProducer

  /**
   * Retrieves the collection of active and pending step requests.
   *
   * @return The collection of information on step requests
   */
  override def stepRequests: Seq[StepRequestInfo] = {
    stepManager.stepRequestList ++ (stepManager match {
      case p: PendingStepSupportLike  => p.pendingStepRequests
      case _                          => Nil
    })
  }

  /**
   * Steps in from the current location to the next line.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepIntoLineWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = createStepFuture(
    stepManager.createStepIntoLineRequest,
    threadInfoProfile,
    extraArguments: _*
  )

  /**
   * Steps over from the current location to the next line.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepOverLineWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = createStepFuture(
    stepManager.createStepOverLineRequest,
    threadInfoProfile,
    extraArguments: _*
  )

  /**
   * Constructs a stream of step events caused by stepping out from the
   * current location to the next line.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepOutLineWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = createStepFuture(
    stepManager.createStepOutLineRequest,
    threadInfoProfile,
    extraArguments: _*
  )

  /**
   * Steps in from the current location to the next location.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepIntoMinWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = createStepFuture(
    stepManager.createStepIntoMinRequest,
    threadInfoProfile,
    extraArguments: _*
  )

  /**
   * Steps over from the current location to the next location.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepOverMinWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = createStepFuture(
    stepManager.createStepOverMinRequest,
    threadInfoProfile,
    extraArguments: _*
  )

  /**
   * Constructs a stream of step events caused by stepping out from the
   * current location to the next location.
   *
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The resulting event and any retrieved data based on
   *         requests from extra arguments
   */
  override def stepOutMinWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = createStepFuture(
    stepManager.createStepOutMinRequest,
    threadInfoProfile,
    extraArguments: _*
  )

  /**
   * Constructs a stream of step events.
   *
   * @param threadInfoProfile The thread with which to receive step events
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of step events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryCreateStepListenerWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepEventAndData]] = Try {
    val JDIArgumentGroup(_, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    newStepPipeline("", (threadInfoProfile, eArgs))
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
  override def isStepRequestPending(
    threadInfoProfile: ThreadInfoProfile
  ): Boolean = {
    lazy val threadReference = threadInfoProfile.toJdiInstance
    stepRequests
      .filter(_.threadReference == threadReference)
      .exists(_.isPending)
  }

  /**
   * Determines if there is any step request for the specified thread with
   * matching arguments that is pending.
   *
   * @param threadInfoProfile The thread with which is receiving the step request
   * @param extraArguments  The additional arguments provided to the specific
   *                        step request
   * @return True if there is at least one step request with the
   *         specified name and arguments in the specified class that is
   *         pending, otherwise false
   */
  override def isStepRequestWithArgsPending(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Boolean = {
    lazy val threadReference = threadInfoProfile.toJdiInstance
    stepRequests
      .filter(t =>
        t.threadReference == threadReference &&
        t.extraArguments == extraArguments
      ).exists(_.isPending)
  }

  /**
   * Removes all step requests for the given thread.
   *
   * @param threadInfoProfile The thread with which is receiving the step request
   * @return The collection of information about removed step requests
   */
  override def removeStepRequests(
    threadInfoProfile: ThreadInfoProfile
  ): Seq[StepRequestInfo] = {
    lazy val threadReference = threadInfoProfile.toJdiInstance
    stepRequests.filter(_.threadReference == threadReference).filter(s =>
      stepManager.removeStepRequestWithId(s.requestId)
    )
  }

  /**
   * Removes all step requests for the given thread with the specified extra
   * arguments.
   *
   * @param threadInfoProfile The thread with which is receiving the step request
   * @param extraArguments  the additional arguments provided to the specific
   *                        step request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeStepRequestWithArgs(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Option[StepRequestInfo] = {
    lazy val threadReference = threadInfoProfile.toJdiInstance
    stepRequests.find(s =>
      s.threadReference == threadReference &&
      s.extraArguments == extraArguments
    ).filter(s =>
      stepManager.removeStepRequestWithId(s.requestId)
    )
  }

  /**
   * Removes all step requests.
   *
   * @return The collection of information about removed step requests
   */
  override def removeAllStepRequests(): Seq[StepRequestInfo] = {
    stepRequests.filter(s =>
      stepManager.removeStepRequestWithId(s.requestId)
    )
  }

  /**
   * Creates a new step request and constructs a future for when its result
   * returns.
   *
   * @param newStepRequestFunc The function used to create the request and
   *                           return the id of the request
   * @param threadInfoProfile The thread in which to perform the step
   * @param extraArguments The additional JDI arguments to provide
   * @return The future containing the result from the step request
   */
  protected def createStepFuture(
    newStepRequestFunc: (ThreadReference, Seq[JDIRequestArgument]) => Try[String],
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    val tryPipeline = {
      val JDIArgumentGroup(rArgs, eArgs, _) =
        JDIArgumentGroup(extraArguments: _*)

      val threadReference = threadInfoProfile.toJdiInstance
      newStepRequestFunc(
        threadReference,
        rArgs :+ ThreadFilter(threadReference)
      ).map(requestId => (requestId, threadInfoProfile, eArgs))
    }.map { case (requestId, thread, eArgs) =>
      newStepPipeline(requestId, (thread, eArgs))
    }

    tryPipelineToFuture(tryPipeline)
  }

  /**
   * Creates a new pipeline of step events. This is not memoized as step events
   * are one-per-thread and are closed after the pipeline's future is completed.
   *
   * @param requestId The id of the step request
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new step event and data pipeline
   */
  protected def newStepPipeline(
    requestId: String,
    args: (ThreadInfoProfile, Seq[JDIEventArgument])
  ): IdentityPipeline[StepEventAndData] = {
    // Lookup final set of request arguments used when creating the request
    val rArgs = stepManager.getStepRequestInfoWithId(requestId)
      .map(_.extraArguments).getOrElse(Nil)
    val eArgs = args._2

    val newPipeline = eventManager
      .addEventDataStream(StepEventType, args._2: _*)
      .map(t => (t._1.asInstanceOf[StepEvent], t._2))
      .map(t => (eventProducer.newStepEventInfoProfile(
        scalaVirtualMachine = scalaVirtualMachine,
        t._1,
        rArgs ++ eArgs: _*
      )(), t._2))
      .noop()

    newPipeline
  }

  /**
   * Converts the try of a pipeline into a future. If the pipeline was created,
   * convert it to a future. Otherwise, convert the failure to an immediate
   * failed future.
   *
   * @param result The attempted pipeline
   * @return The future representing the attempted pipeline's
   */
  protected def tryPipelineToFuture(
    result: Try[IdentityPipeline[StepEventAndData]]
  ): Future[StepEventAndData] = result match {
    case Success(pipeline)  => pipeline.toFuture
    case Failure(error)     => Future.failed(error)
  }
}
