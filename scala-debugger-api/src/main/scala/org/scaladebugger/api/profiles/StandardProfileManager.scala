package org.scaladebugger.api.profiles
//import acyclic.file

import java.util.concurrent.ConcurrentHashMap

import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.scala210.Scala210DebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile

import scala.collection.JavaConverters._

/**
 * Represents the standard implementation of the profile manager.
 */
class StandardProfileManager extends ProfileManager {
  /** Contains a mapping from profile name to instance. */
  private val profiles = new ConcurrentHashMap[String, DebugProfile]().asScala

  /**
   * Registers the profile using the provided name. Ignores any registration
   * under an already-used name.
   *
   * @param name The name of the profile to register
   * @param profile The profile to register
   */
  def register(name: String, profile: DebugProfile): Option[DebugProfile] = {
    require(name != null, "Profile name cannot be null!")
    require(name.nonEmpty, "Profile name cannot be empty!")
    require(profile != null, "Profile cannot be null!")

    val success = profiles.putIfAbsent(name, profile).isEmpty

    if (success) Some(profile)
    else None
  }

  /**
   * Unregisters the profile with the provided name.
   *
   * @param name The name of the profile to unregister
   *
   * @return Some debug profile if unregistered, otherwise None
   */
  def unregister(name: String): Option[DebugProfile] = {
    profiles.remove(name)
  }

  /**
   * Retrieves the profile with the provided name.
   *
   * @param name The name of the profile to retrieve
   *
   * @return Some debug profile if found, otherwise None
   */
  def retrieve(name: String): Option[DebugProfile] = {
    profiles.get(name)
  }
}

object StandardProfileManager {
  /**
   * Creates a new instance of the profile manager with default profiles
   * already registered.
   *
   * @note Currently, the pure debug profile has its virtual machine set to
   *       null, which makes operations like retrieving the main class name
   *       and arguments impossible. Do not use those methods with this
   *       default profile manager!
   *
   * @param managerContainer The container of managers to use with this new
   *                         profile manager
   *
   * @return The new profile manager
   */
  def newDefaultInstance(
    managerContainer: ManagerContainer = ManagerContainer.usingDummyManagers()
  ): StandardProfileManager = {
    val profileManager = new StandardProfileManager

    // TODO: Refactor PureDebugProfile to not need virtual machine as providing
    //       null will cause usage of it to fail
    profileManager.register(PureDebugProfile.Name, new PureDebugProfile(
      null, managerContainer
    )(_virtualMachine = null))
    profileManager.register(Scala210DebugProfile.Name, new Scala210DebugProfile(
      null, managerContainer
    )(_virtualMachine = null))

    profileManager
  }
}
