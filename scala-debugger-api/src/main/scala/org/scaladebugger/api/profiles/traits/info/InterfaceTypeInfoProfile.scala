package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.InterfaceType

import scala.util.Try

/**
 * Represents the interface for retrieving interface type-based information.
 */
trait InterfaceTypeInfoProfile extends ReferenceTypeInfoProfile with TypeInfoProfile {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: InterfaceTypeInfoProfile

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: InterfaceType

  /**
   * Returns the prepared classes which directly implement this interface.
   *
   * @return The collection of class type info profiles
   */
  def implementors: Seq[ClassTypeInfoProfile]

  /**
   * Returns the prepared interfaces which directly extend this interface.
   *
   * @return The collection of interface type info profiles
   */
  def subinterfaces: Seq[InterfaceTypeInfoProfile]

  /**
   * Returns the interfaces directly extended by this interface.
   *
   * @return The collection of interface type info profiles
   */
  def superinterfaces: Seq[InterfaceTypeInfoProfile]

  /**
   * Returns the interfaces directly extended by this interface.
   *
   * @return Success containing the collection of interface type info profiles,
   *         otherwise a failure
   */
  def trySuperinterfaces: Try[Seq[InterfaceTypeInfoProfile]] =
    Try(superinterfaces)
}
