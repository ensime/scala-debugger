package org.scaladebugger.api.profiles.swappable.requests.methods
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.methods.MethodEntryRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.methods.MethodEntryProfile

import scala.util.Try

/**
 * Represents a swappable profile for method entry events that redirects the
 * invocation to another profile.
 */
trait SwappableMethodEntryProfile extends MethodEntryProfile {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateMethodEntryRequestWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodEntryEventAndData]] = {
    withCurrentProfile.tryGetOrCreateMethodEntryRequestWithData(
      className,
      methodName,
      extraArguments: _*
    )
  }

  override def isMethodEntryRequestPending(
    className: String,
    methodName: String
  ): Boolean = {
    withCurrentProfile.isMethodEntryRequestPending(className, methodName)
  }

  override def isMethodEntryRequestWithArgsPending(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isMethodEntryRequestWithArgsPending(
      className,
      methodName,
      extraArguments: _*
    )
  }

  override def removeMethodEntryRequests(
    className: String,
    methodName: String
  ): Seq[MethodEntryRequestInfo] = {
    withCurrentProfile.removeMethodEntryRequests(
      className,
      methodName
    )
  }

  override def removeMethodEntryRequestWithArgs(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Option[MethodEntryRequestInfo] = {
    withCurrentProfile.removeMethodEntryRequestWithArgs(
      className,
      methodName,
      extraArguments: _*
    )
  }

  override def removeAllMethodEntryRequests(): Seq[MethodEntryRequestInfo] = {
    withCurrentProfile.removeAllMethodEntryRequests()
  }

  override def methodEntryRequests: Seq[MethodEntryRequestInfo] = {
    withCurrentProfile.methodEntryRequests
  }
}
