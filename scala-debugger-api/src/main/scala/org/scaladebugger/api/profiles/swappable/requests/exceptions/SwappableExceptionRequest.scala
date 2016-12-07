package org.scaladebugger.api.profiles.swappable.requests.exceptions

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.exceptions.ExceptionRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.exceptions.ExceptionRequest

import scala.util.Try

/**
 * Represents a swappable profile for exception events that redirects the
 * invocation to another profile.
 */
trait SwappableExceptionRequest extends ExceptionRequest {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateExceptionRequestWithData(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventAndData]] = {
    withCurrentProfile.tryGetOrCreateExceptionRequestWithData(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )
  }

  override def tryGetOrCreateAllExceptionsRequestWithData(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ExceptionEventAndData]] = {
    withCurrentProfile.tryGetOrCreateAllExceptionsRequestWithData(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )
  }

  override def isAllExceptionsRequestPending: Boolean = {
    withCurrentProfile.isAllExceptionsRequestPending
  }

  override def isAllExceptionsRequestWithArgsPending(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isAllExceptionsRequestWithArgsPending(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )
  }

  override def isExceptionRequestPending(exceptionName: String): Boolean = {
    withCurrentProfile.isExceptionRequestPending(exceptionName)
  }

  override def isExceptionRequestWithArgsPending(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isExceptionRequestWithArgsPending(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )
  }

  override def removeOnlyAllExceptionsRequests(): Seq[ExceptionRequestInfo] = {
    withCurrentProfile.removeOnlyAllExceptionsRequests()
  }

  override def removeOnlyAllExceptionsRequestWithArgs(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Option[ExceptionRequestInfo] = {
    withCurrentProfile.removeOnlyAllExceptionsRequestWithArgs(
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )
  }

  override def removeExceptionRequests(
    exceptionName: String
  ): Seq[ExceptionRequestInfo] = {
    withCurrentProfile.removeExceptionRequests(exceptionName)
  }

  override def removeExceptionRequestWithArgs(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIArgument*
  ): Option[ExceptionRequestInfo] = {
    withCurrentProfile.removeExceptionRequestWithArgs(
      exceptionName,
      notifyCaught,
      notifyUncaught,
      extraArguments: _*
    )
  }

  override def removeAllExceptionRequests(): Seq[ExceptionRequestInfo] = {
    withCurrentProfile.removeAllExceptionRequests()
  }

  override def exceptionRequests: Seq[ExceptionRequestInfo] = {
    withCurrentProfile.exceptionRequests
  }
}
