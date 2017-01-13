package org.scaladebugger.api.profiles.traits.info

/**
 * Represents an exception that occurs when a local value is trying to be
 * cast to another value using remote type information, but casting to the
 * desired type is not possible.
 *
 * @param value The local value the could not be casted
 * @param `type` The type information that tried to perform the cast
 */
class CastNotPossibleException(
  private val value: Any,
  private val `type`: TypeInfo
) extends Exception(
  s"Not possible to cast ${value.getClass.getName} to ${`type`.name}"
)
