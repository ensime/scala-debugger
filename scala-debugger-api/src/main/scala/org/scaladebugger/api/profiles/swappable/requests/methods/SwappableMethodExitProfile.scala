package org.scaladebugger.api.profiles.swappable.requests.methods
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.methods.MethodExitRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.methods.MethodExitProfile

import scala.util.Try

/**
 * Represents a swappable profile for method exit events that redirects the
 * invocation to another profile.
 */
trait SwappableMethodExitProfile extends MethodExitProfile {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateMethodExitRequestWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodExitEventAndData]] = {
    withCurrentProfile.tryGetOrCreateMethodExitRequestWithData(
      className,
      methodName,
      extraArguments: _*
    )
  }

  override def isMethodExitRequestPending(
    className: String,
    methodName: String
  ): Boolean = {
    withCurrentProfile.isMethodExitRequestPending(className, methodName)
  }

  override def isMethodExitRequestWithArgsPending(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isMethodExitRequestWithArgsPending(
      className,
      methodName,
      extraArguments: _*
    )
  }

  override def removeMethodExitRequests(
    className: String,
    methodName: String
  ): Seq[MethodExitRequestInfo] = {
    withCurrentProfile.removeMethodExitRequests(
      className,
      methodName
    )
  }

  override def removeMethodExitRequestWithArgs(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Option[MethodExitRequestInfo] = {
    withCurrentProfile.removeMethodExitRequestWithArgs(
      className,
      methodName,
      extraArguments: _*
    )
  }

  override def removeAllMethodExitRequests(): Seq[MethodExitRequestInfo] = {
    withCurrentProfile.removeAllMethodExitRequests()
  }

  override def methodExitRequests: Seq[MethodExitRequestInfo] = {
    withCurrentProfile.methodExitRequests
  }
}
