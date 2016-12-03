package org.scaladebugger.api.profiles
//import acyclic.file

import java.util.concurrent.ConcurrentHashMap

import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.scala210.Scala210DebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

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
  /** TODO: Write docs */
  def registerDefaultProfiles(
    profileManager: ProfileManager,
    scalaVirtualMachine: ScalaVirtualMachine,
    managerContainer: ManagerContainer = ManagerContainer.usingDummyManagers()
  ): ProfileManager = {
    // TODO: Refactor PureDebugProfile to not need virtual machine as providing
    //       null will cause usage of it to fail
    profileManager.register(PureDebugProfile.Name, new PureDebugProfile(
      scalaVirtualMachine, managerContainer
    )(_virtualMachine = scalaVirtualMachine.underlyingVirtualMachine))
    profileManager.register(Scala210DebugProfile.Name, new Scala210DebugProfile(
      scalaVirtualMachine, managerContainer
    )(_virtualMachine = scalaVirtualMachine.underlyingVirtualMachine))

    profileManager
  }
}
