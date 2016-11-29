package org.scaladebugger.api.profiles.swappable.requests.classes
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.classes.ClassPrepareRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.classes.ClassPrepareProfile

import scala.util.Try

/**
 * Represents a swappable profile for class prepare events that redirects the
 * invocation to another profile.
 */
trait SwappableClassPrepareProfile extends ClassPrepareProfile {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateClassPrepareRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassPrepareEventAndData]] = {
    withCurrentProfile.tryGetOrCreateClassPrepareRequestWithData(extraArguments: _*)
  }

  override def isClassPrepareRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isClassPrepareRequestWithArgsPending(extraArguments: _*)
  }

  override def removeClassPrepareRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[ClassPrepareRequestInfo] = {
    withCurrentProfile.removeClassPrepareRequestWithArgs(extraArguments: _*)
  }

  override def removeAllClassPrepareRequests(): Seq[ClassPrepareRequestInfo] = {
    withCurrentProfile.removeAllClassPrepareRequests()
  }

  override def classPrepareRequests: Seq[ClassPrepareRequestInfo] = {
    withCurrentProfile.classPrepareRequests
  }
}
