package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ReferenceType

import scala.util.Try

/**
 * Represents the interface for "reference type"-based interaction.
 */
trait ReferenceTypeInfoProfile extends CommonInfoProfile with TypeInfoProfile {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: ReferenceTypeInfoProfile

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ReferenceType

  /**
   * Retrieves all fields declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return The collection of fields as variable info profiles
   */
  def allFields: Seq[FieldVariableInfoProfile]

  /**
   * Retrieves all fields declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return Success containing the collection of fields as variable info
   *         profiles, otherwise a failure
   */
  def tryAllFields: Try[Seq[FieldVariableInfoProfile]] = Try(allFields)

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included.
   *
   * @return The collection of fields as variable info profiles
   */
  def visibleFields: Seq[FieldVariableInfoProfile]

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included.
   *
   * @return Success containing the collection of fields as variable info
   *         profiles, otherwise a failure
   */
  def tryVisibleFields: Try[Seq[FieldVariableInfoProfile]] = Try(visibleFields)

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included. Offset index
   * information is included.
   *
   * @return The collection of fields as variable info profiles
   */
  def indexedVisibleFields: Seq[FieldVariableInfoProfile]

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included. Offset index
   * informatin is included.
   *
   * @return Success containing the collection of fields as variable info
   *         profiles, otherwise a failure
   */
  def tryIndexedVisibleFields: Try[Seq[FieldVariableInfoProfile]] =
    Try(indexedVisibleFields)

  /**
   * Retrieves the visible field with the matching name with offset index
   * information.
   *
   * @param name The name of the field to retrieve
   * @return The field as a variable info profile
   */
  @throws[NoSuchElementException]
  def indexedField(name: String): FieldVariableInfoProfile =
    indexedFieldOption(name).get

  /**
   * Retrieves the visible field with the matching name with offset index
   * information.
   *
   * @param name The name of the field to retrieve
   * @return Some field as a variable info profile, or None if doesn't exist
   */
  def indexedFieldOption(name: String): Option[FieldVariableInfoProfile]

  /**
   * Retrieves the visible field with the matching name with offset index
   * information.
   *
   * @param name The name of the field to retrieve
   * @return Success containing the field as a variable info profile, otherwise
   *         a failure
   */
  def tryIndexedField(name: String): Try[FieldVariableInfoProfile] =
    Try(indexedField(name))

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return Some field as a variable info profile, or None if doesn't exist
   */
  def fieldOption(name: String): Option[FieldVariableInfoProfile]

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return The field as a variable info profile
   */
  @throws[NoSuchElementException]
  def field(name: String): FieldVariableInfoProfile = fieldOption(name).get

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return Success containing the field as a variable info profile, otherwise
   *         a failure
   */
  def tryField(name: String): Try[FieldVariableInfoProfile] = Try(field(name))

  /**
   * Retrieves all methods declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return The collection of methods as method info profiles
   */
  def allMethods: Seq[MethodInfoProfile]

  /**
   * Retrieves all methods declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return Success containing the collection of methods as method info
   *         profiles, otherwise a failure
   */
  def tryAllMethods: Try[Seq[MethodInfoProfile]] = Try(allMethods)

  /**
   * Retrieves unhidden and unambiguous methods in this type. Methods hidden
   * by other methods with the same name (in a more recently inherited class)
   * are not included. Methods that are ambiguously multiply inherited are also
   * not included. All other inherited methods are included.
   *
   * @return The collection of methods as method info profiles
   */
  def visibleMethods: Seq[MethodInfoProfile]

  /**
   * Retrieves unhidden and unambiguous methods in this type. Methods hidden
   * by other methods with the same name (in a more recently inherited class)
   * are not included. Methods that are ambiguously multiply inherited are also
   * not included. All other inherited methods are included.
   *
   * @return Success containing the collection of methods as method info
   *         profiles, otherwise a failure
   */
  def tryVisibleMethods: Try[Seq[MethodInfoProfile]] = Try(visibleMethods)

  /**
   * Retrieves the visible methods with the matching name.
   *
   * @param name The name of the method to retrieve
   * @return The collection of method info profiles
   */
  def methods(name: String): Seq[MethodInfoProfile]

  /**
   * Retrieves the visible method with the matching name.
   *
   * @param name The name of the method to retrieve
   * @return Success containing the method as a method info profile, otherwise
   *         a failure
   */
  def tryMethods(name: String): Try[Seq[MethodInfoProfile]] =
    Try(methods(name))

  /**
   * Retrieves the classloader object which loaded the class associated with
   * this type.
   *
   * @return The profile representing the classloader
   */
  @throws[NoSuchElementException]
  def classLoader: ClassLoaderInfoProfile = classLoaderOption.get

  /**
   * Retrieves the classloader object which loaded the class associated with
   * this type.
   *
   * @return Some profile representing the classloader,
   *         otherwise None if loaded through the bootstrap classloader
   */
  def classLoaderOption: Option[ClassLoaderInfoProfile]

  /**
   * Retrieves the class object associated with this type.
   *
   * @return The profile representing the class
   */
  def classObject: ClassObjectInfoProfile

  /**
   * Retrieves the generic signature type if it exists.
   *
   * @return Some signature if it exists, otherwise None
   */
  def genericSignature: Option[String]

  /**
   * Retrieves reachable instances of this type.
   *
   * @param maxInstances The maximum number of instances to return, or zero
   *                     to get all reachable instances
   * @return The collection of object instances
   */
  def instances(maxInstances: Long): Seq[ObjectInfoProfile]

  /**
   * Retrieves reachable instances of this type.
   *
   * @param maxInstances The maximum number of instances to return, or zero
   *                     to get all reachable instances
   * @return Success containing the collection of object instances, otherwise
   *         a failure
   */
  def tryInstances(maxInstances: Long): Try[Seq[ObjectInfoProfile]] =
    Try(instances(maxInstances))

  /**
   * Retrieves all reachable instances of this type.
   *
   * @return The collection of object instances
   */
  def allInstances: Seq[ObjectInfoProfile] = instances(0)

  /**
   * Retrieves all reachable instances of this type.
   *
   * @return Success containing the collection of object instances, otherwise
   *         a failure
   */
  def tryAllInstances: Try[Seq[ObjectInfoProfile]] = Try(allInstances)

  /**
   * Indicates whether or not this type is abstract.
   *
   * @return True if abstract, otherwise false
   */
  def isAbstract: Boolean

  /**
   * Indicates whether or not this type is final.
   *
   * @return True if final, otherwise false
   */
  def isFinal: Boolean

  /**
   * Indicates whether or not this type has been initialized. This value is
   * the same as isPrepared for interfaces and is undefined for arrays and
   * primitive types.
   *
   * @return True if initialized, otherwise false
   */
  def isInitialized: Boolean

  /**
   * Indicates whether or not this type's class has been prepared.
   *
   * @return True if prepared, otherwise false
   */
  def isPrepared: Boolean

  /**
   * Indicates whether or not this type is static.
   *
   * @return True if static, otherwise false
   */
  def isStatic: Boolean

  /**
   * Indicates whether or not this type has been verified. This value is
   * the same as isPrepared for interfaces and is undefined for arrays and
   * primitive types.
   *
   * @return True if verified, otherwise false
   */
  def isVerified: Boolean

  /**
   * Retrieves and returns all valid locations for executable lines within
   * this type.
   *
   * @return The collection of location information
   */
  def allLineLocations: Seq[LocationInfoProfile]

  /**
   * Retrieves and returns all valid locations for executable lines within
   * this type.
   *
   * @return Success containing the collection of location information,
   *         otherwise a failure
   */
  def tryAllLineLocations: Try[Seq[LocationInfoProfile]] =
    Try(allLineLocations)

  /**
   * Retrieves and returns all valid locations for a specific executable line
   * within this type.
   *
   * @return The collection of location information
   */
  def locationsOfLine(line: Int): Seq[LocationInfoProfile]

  /**
   * Retrieves and returns all valid locations for a specific executable line
   * within this type.
   *
   * @return Success containing the collection of location information,
   *         otherwise a failure
   */
  def tryLocationsOfLine(line: Int): Try[Seq[LocationInfoProfile]] =
    Try(locationsOfLine(line))

  /**
   * Retrieves the major class version number defined in the class file format
   * of the JVM specification.
   *
   * @return The major version number
   */
  def majorVersion: Int

  /**
   * Retrieves the major class version number defined in the class file format
   * of the JVM specification.
   *
   * @return Success containing the major version number, otherwise a failure
   */
  def tryMajorVersion: Try[Int] = Try(majorVersion)

  /**
   * Retrieves the minor class version number defined in the class file format
   * of the JVM specification.
   *
   * @return The minor version number
   */
  def minorVersion: Int

  /**
   * Retrieves the minor class version number defined in the class file format
   * of the JVM specification.
   *
   * @return Success containing the minor version number, otherwise a failure
   */
  def tryMinorVersion: Try[Int] = Try(minorVersion)

  /**
   * Retrieves the fully-qualified class name of this type.
   *
   * @return The fully-qualified class name
   */
  def name: String

  /**
   * Retrieves reference type information for all types declared inside this
   * type.
   *
   * @return The collection of reference type information
   */
  def nestedTypes: Seq[ReferenceTypeInfoProfile]

  /**
   * Retrieves the source debug extension for this type.
   *
   * @return The source debug extension
   */
  def sourceDebugExtension: String

  /**
   * Retrieves the source debug extension for this type.
   *
   * @return Success containing the source debug extension, otherwise a failure
   */
  def trySourceDebugExtension: Try[String] = Try(sourceDebugExtension)

  /**
   * Retrieves all identifying names for the source(s) corresponding to this
   * type.
   *
   * @return The collection of identifying names
   */
  def sourceNames: Seq[String]

  /**
   * Retrieves all identifying names for the source(s) corresponding to this
   * type.
   *
   * @return Success containing the collection of identifying names, otherwise
   *         a failure
   */
  def trySourceNames: Try[Seq[String]] = Try(sourceNames)

  /**
   * Retrieves all source paths corresponding to this type.
   *
   * @return The collection of source paths
   */
  def sourcePaths: Seq[String]

  /**
   * Retrieves all source paths corresponding to this type.
   *
   * @return Success containing the collection of source paths, otherwise a
   *         failure
   */
  def trySourcePaths: Try[Seq[String]] = Try(sourcePaths)

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    this.name
  }
}
