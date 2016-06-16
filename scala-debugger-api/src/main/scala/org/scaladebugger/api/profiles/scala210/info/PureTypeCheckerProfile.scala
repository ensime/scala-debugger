package org.scaladebugger.api.profiles.scala210.info

import org.scaladebugger.api.profiles.traits.info.TypeCheckerProfile

/**
 * Represents a pure implementation of a type checker profile that adds no
 * custom logic on top of the standard JDI.
 */
class PureTypeCheckerProfile extends TypeCheckerProfile {
  /**
   * Compares type names to see if they are equivalent, converting java classes
   * boxing primitive types into the equivalent primitive types.
   *
   * @param typeName1 The first type name as a string
   * @param typeName2 The second type name as a string
   * @return True if the types are equivalent, otherwise false
   */
  override def equalTypeNames(typeName1: String, typeName2: String): Boolean = {
    def convertPrimitive(typeName: String): String = typeName match {
      case "java.lang.Boolean"  => "boolean"
      case "java.lang.Byte"     => "byte"
      case "java.lang.Char"     => "char"
      case "java.lang.Integer"  => "int"
      case "java.lang.Short"    => "short"
      case "java.lang.Long"     => "long"
      case "java.lang.Float"    => "float"
      case "java.lang.Double"   => "double"
      case _                    => typeName
    }

    convertPrimitive(typeName1.trim) == convertPrimitive(typeName2.trim)
  }
}
