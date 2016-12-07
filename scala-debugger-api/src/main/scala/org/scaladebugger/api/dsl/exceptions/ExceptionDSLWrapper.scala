package org.scaladebugger.api.dsl.exceptions

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.requests.exceptions.ExceptionRequest
import org.scaladebugger.api.profiles.traits.info.events.ExceptionEventInfo

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param exceptionProfile The profile to wrap
 */
class ExceptionDSLWrapper private[dsl] (
  private val exceptionProfile: ExceptionRequest
) {
  /** Represents a Exception event and any associated data. */
  type ExceptionEventAndData = (ExceptionEventInfo, Seq[JDIEventDataResult])

  /** @see ExceptionRequest#tryGetOrCreateExceptionRequest(String, Boolean, Boolean, JDIArgument*) */
  def onException(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventInfo]] =
    exceptionProfile.tryGetOrCreateExceptionRequest(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionRequest#getOrCreateExceptionRequest(String, Boolean, Boolean, JDIArgument*) */
  def onUnsafeException(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEventInfo] =
    exceptionProfile.getOrCreateExceptionRequest(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionRequest#getOrCreateExceptionRequestWithData(String, Boolean, Boolean, JDIArgument*) */
  def onUnsafeExceptionWithData(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEventAndData] =
    exceptionProfile.getOrCreateExceptionRequestWithData(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionRequest#tryGetOrCreateExceptionRequestWithData(String, Boolean, Boolean, JDIArgument*) */
  def onExceptionWithData(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventAndData]] =
    exceptionProfile.tryGetOrCreateExceptionRequestWithData(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionRequest#tryGetOrCreateAllExceptionsRequest(Boolean, Boolean, JDIArgument*) */
  def onAllExceptions(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventInfo]] =
    exceptionProfile.tryGetOrCreateAllExceptionsRequest(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionRequest#getOrCreateAllExceptionsRequest(Boolean, Boolean, JDIArgument*) */
  def onUnsafeAllExceptions(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEventInfo] =
    exceptionProfile.getOrCreateAllExceptionsRequest(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionRequest#getOrCreateAllExceptionsRequestWithData(Boolean, Boolean, JDIArgument*) */
  def onUnsafeAllExceptionsWithData(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ExceptionEventAndData] =
    exceptionProfile.getOrCreateAllExceptionsRequestWithData(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )

  /** @see ExceptionRequest#tryGetOrCreateAllExceptionsRequestWithData(Boolean, Boolean, JDIArgument*) */
  def onAllExceptionsWithData(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventAndData]] =
    exceptionProfile.tryGetOrCreateAllExceptionsRequestWithData(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )
}
