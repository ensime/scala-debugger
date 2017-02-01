package org.scaladebugger.api.profiles.frozen.info

import org.scaladebugger.api.profiles.frozen.FrozenException
import org.scaladebugger.api.profiles.traits.info._

import scala.util.Try

/**
 * Represents type information frozen (snapshot at a point in time) for a remote
 * JVM.
 */
trait FrozenReferenceTypeInfoLike extends ReferenceTypeInfo with FrozenTypeInfoLike {
  protected val _allFields: Try[Seq[FieldVariableInfo]]
  protected val _visibleFields: Try[Seq[FieldVariableInfo]]
  protected val _indexedVisibleFields: Try[Seq[FieldVariableInfo]]

  /**
   * Retrieves all fields declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return The collection of fields as variable info profiles
   */
  @throws[FrozenException]
  override def allFields: Seq[FieldVariableInfo] = _allFields.get

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included.
   *
   * @return The collection of fields as variable info profiles
   */
  @throws[FrozenException]
  override def visibleFields: Seq[FieldVariableInfo] = _visibleFields.get

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included. Offset index
   * information is included.
   *
   * @return The collection of fields as variable info profiles
   */
  override def indexedVisibleFields: Seq[FieldVariableInfo] =
    _indexedVisibleFields.get

  /**
   * Retrieves the visible field with the matching name with offset index
   * information.
   *
   * @param name The name of the field to retrieve
   * @return Some field as a variable info profile, or None if doesn't exist
   */
  override def indexedFieldOption(name: String): Option[FieldVariableInfo] =
    _indexedVisibleFields.get.find(_.name == name)

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return Some field as a variable info profile, or None if doesn't exist
   */
  override def fieldOption(name: String): Option[FieldVariableInfo] =
    _visibleFields.get.find(_.name == name)

  /**
   * Retrieves all methods declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return The collection of methods as method info profiles
   */
  override def allMethods: Seq[MethodInfo] = ???

  /**
   * Retrieves unhidden and unambiguous methods in this type. Methods hidden
   * by other methods with the same name (in a more recently inherited class)
   * are not included. Methods that are ambiguously multiply inherited are also
   * not included. All other inherited methods are included.
   *
   * @return The collection of methods as method info profiles
   */
  override def visibleMethods: Seq[MethodInfo] = ???

  /**
   * Retrieves the visible methods with the matching name.
   *
   * @param name The name of the method to retrieve
   * @return The collection of method info profiles
   */
  override def methods(name: String): Seq[MethodInfo] = ???

  /**
   * Retrieves the classloader object which loaded the class associated with
   * this type.
   *
   * @return Some profile representing the classloader,
   *         otherwise None if loaded through the bootstrap classloader
   */
  override def classLoaderOption: Option[ClassLoaderInfo] = ???

  /**
   * Retrieves the class object associated with this type.
   *
   * @return The profile representing the class
   */
  override def classObject: ClassObjectInfo = ???

  /**
   * Retrieves the generic signature type if it exists.
   *
   * @return Some signature if it exists, otherwise None
   */
  override def genericSignature: Option[String] = ???

  /**
   * Retrieves reachable instances of this type.
   *
   * @param maxInstances The maximum number of instances to return, or zero
   *                     to get all reachable instances
   * @return The collection of object instances
   */
  override def instances(maxInstances: Long): Seq[ObjectInfo] = ???

  /**
   * Indicates whether or not this type is abstract.
   *
   * @return True if abstract, otherwise false
   */
  override def isAbstract: Boolean = ???

  /**
   * Indicates whether or not this type is final.
   *
   * @return True if final, otherwise false
   */
  override def isFinal: Boolean = ???

  /**
   * Indicates whether or not this type has been initialized. This value is
   * the same as isPrepared for interfaces and is undefined for arrays and
   * primitive types.
   *
   * @return True if initialized, otherwise false
   */
  override def isInitialized: Boolean = ???

  /**
   * Indicates whether or not this type's class has been prepared.
   *
   * @return True if prepared, otherwise false
   */
  override def isPrepared: Boolean = ???

  /**
   * Indicates whether or not this type is static.
   *
   * @return True if static, otherwise false
   */
  override def isStatic: Boolean = ???

  /**
   * Indicates whether or not this type has been verified. This value is
   * the same as isPrepared for interfaces and is undefined for arrays and
   * primitive types.
   *
   * @return True if verified, otherwise false
   */
  override def isVerified: Boolean = ???

  /**
   * Retrieves and returns all valid locations for executable lines within
   * this type.
   *
   * @return The collection of location information
   */
  override def allLineLocations: Seq[LocationInfo] = ???

  /**
   * Retrieves and returns all valid locations for a specific executable line
   * within this type.
   *
   * @return The collection of location information
   */
  override def locationsOfLine(line: Int): Seq[LocationInfo] = ???

  /**
   * Retrieves the major class version number defined in the class file format
   * of the JVM specification.
   *
   * @return The major version number
   */
  override def majorVersion: Int = ???

  /**
   * Retrieves the minor class version number defined in the class file format
   * of the JVM specification.
   *
   * @return The minor version number
   */
  override def minorVersion: Int = ???

  /**
   * Retrieves reference type information for all types declared inside this
   * type.
   *
   * @return The collection of reference type information
   */
  override def nestedTypes: Seq[ReferenceTypeInfo] = ???

  /**
   * Retrieves the source debug extension for this type.
   *
   * @return The source debug extension
   */
  override def sourceDebugExtension: String = ???

  /**
   * Retrieves all identifying names for the source(s) corresponding to this
   * type.
   *
   * @return The collection of identifying names
   */
  override def sourceNames: Seq[String] = ???

  /**
   * Retrieves all source paths corresponding to this type.
   *
   * @return The collection of source paths
   */
  override def sourcePaths: Seq[String] = ???
}
