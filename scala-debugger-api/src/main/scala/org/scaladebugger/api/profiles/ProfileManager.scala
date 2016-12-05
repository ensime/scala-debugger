package org.scaladebugger.api.profiles

import org.scaladebugger.api.profiles.traits.DebugProfile

/**
 * Represents a manger for available debug profiles.
 */
trait ProfileManager {
  /**
   * Registers the profile using the provided name. Ignores any registration
   * under an already-used name.
   *
   * @param name The name of the profile to register
   * @param profile The profile to register
   */
  def register(name: String, profile: DebugProfile): Option[DebugProfile]

  /**
   * Unregisters the profile with the provided name.
   *
   * @param name The name of the profile to unregister
   *
   * @return Some debug profile if unregistered, otherwise None
   */
  def unregister(name: String): Option[DebugProfile]

  /**
   * Retrieves the profile with the provided name.
   *
   * @param name The name of the profile to retrieve
   *
   * @return Some debug profile if found, otherwise None
   */
  def retrieve(name: String): Option[DebugProfile]
}


