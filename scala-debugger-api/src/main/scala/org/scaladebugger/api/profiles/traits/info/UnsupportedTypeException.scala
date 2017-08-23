package org.scaladebugger.api.profiles.traits.info
import acyclic.file

/**
 * Represents an exception that occurs when a local value is trying to be
 * sent remotely, but the local value has a type that is unsupported for
 * remote creation.
 *
 * @param value The local value with the unsupported type
 */
class UnsupportedTypeException(private val value: Any)
  extends Exception(s"Unsupported type: ${value.getClass.getName}")
