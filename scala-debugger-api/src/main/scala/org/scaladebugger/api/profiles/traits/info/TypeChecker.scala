package org.scaladebugger.api.profiles.traits.info
import acyclic.file

/**
 * Represents the interface for type-checking based interactions.
 */
trait TypeChecker {
  /**
   * Compares type names to see if they are equivalent.
   *
   * @param typeName1 The first type name as a string
   * @param typeName2 The second type name as a string
   * @return True if the types are equivalent, otherwise false
   */
  def equalTypeNames(typeName1: String, typeName2: String): Boolean
}
