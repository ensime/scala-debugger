package org.scaladebugger.api.profiles.swappable.requests.vm
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.vm.VMDisconnectProfile

import scala.util.Try

/**
 * Represents a swappable profile for vm disconnect events that redirects the
 * invocation to another profile.
 */
trait SwappableVMDisconnectProfile extends VMDisconnectProfile {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateVMDisconnectRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDisconnectEventAndData]] = {
    withCurrentProfile.tryGetOrCreateVMDisconnectRequestWithData(extraArguments: _*)
  }
}
