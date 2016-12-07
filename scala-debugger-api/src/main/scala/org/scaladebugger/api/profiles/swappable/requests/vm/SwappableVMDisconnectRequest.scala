package org.scaladebugger.api.profiles.swappable.requests.vm

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.vm.VMDisconnectRequest

import scala.util.Try

/**
 * Represents a swappable profile for vm disconnect events that redirects the
 * invocation to another profile.
 */
trait SwappableVMDisconnectRequest extends VMDisconnectRequest {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateVMDisconnectRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDisconnectEventAndData]] = {
    withCurrentProfile.tryGetOrCreateVMDisconnectRequestWithData(extraArguments: _*)
  }
}
