package org.scaladebugger.api.profiles.swappable.info

//import acyclic.file
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.info.{CreateInfoProfile, MiscInfoProfile, ValueInfoProfile}

/**
 * Represents a swappable profile for creating data that redirects the
 * invocation to another profile.
 */
trait SwappableCreateInfoProfile extends CreateInfoProfile {
  this: SwappableDebugProfileManagement =>

  override def createRemotely(value: AnyVal): ValueInfoProfile = {
    withCurrentProfile.createRemotely(value)
  }

  override def createRemotely(value: String): ValueInfoProfile = {
    withCurrentProfile.createRemotely(value)
  }
}
