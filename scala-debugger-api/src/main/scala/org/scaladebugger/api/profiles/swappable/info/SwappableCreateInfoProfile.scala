package org.scaladebugger.api.profiles.swappable.info

import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.info.{CreateInfoProfile, MiscInfo, ValueInfo}

/**
 * Represents a swappable profile for creating data that redirects the
 * invocation to another profile.
 */
trait SwappableCreateInfoProfile extends CreateInfoProfile {
  this: SwappableDebugProfileManagement =>

  override def createRemotely(value: AnyVal): ValueInfo = {
    withCurrentProfile.createRemotely(value)
  }

  override def createRemotely(value: String): ValueInfo = {
    withCurrentProfile.createRemotely(value)
  }
}
