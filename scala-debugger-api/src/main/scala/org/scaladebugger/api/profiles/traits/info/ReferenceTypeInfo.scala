package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ReferenceType
import org.scaladebugger.macros.freeze.FreezeMetadata.ReturnType
import org.scaladebugger.macros.freeze.{CanFreeze, CannotFreeze, Freezable, FreezeMetadata}

import scala.util.Try

/**
 * Represents the interface for "reference type"-based interaction.
 */
@Freezable
trait ReferenceTypeInfo extends CommonInfo with TypeInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  @CannotFreeze
  override def toJavaInfo: ReferenceTypeInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  @CannotFreeze
  override def toJdiInstance: ReferenceType

  /**
   * Retrieves all fields declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return The collection of fields as variable info profiles
   */
  @FreezeMetadata(ReturnType.FreezeCollection)
  @CanFreeze
  def allFields: Seq[FieldVariableInfo]

  /**
   * Retrieves all fields declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return Success containing the collection of fields as variable info
   *         profiles, otherwise a failure
   */
  def tryAllFields: Try[Seq[FieldVariableInfo]] = Try(allFields)

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included.
   *
   * @return The collection of fields as variable info profiles
   */
  @FreezeMetadata(ReturnType.FreezeCollection)
  @CanFreeze
  def visibleFields: Seq[FieldVariableInfo]

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included.
   *
   * @return Success containing the collection of fields as variable info
   *         profiles, otherwise a failure
   */
  def tryVisibleFields: Try[Seq[FieldVariableInfo]] = Try(visibleFields)

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included. Offset index
   * information is included.
   *
   * @return The collection of fields as variable info profiles
   */
  @FreezeMetadata(ReturnType.FreezeCollection)
  @CanFreeze
  def indexedVisibleFields: Seq[FieldVariableInfo]

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
  def tryIndexedVisibleFields: Try[Seq[FieldVariableInfo]] =
    Try(indexedVisibleFields)

  /**
   * Retrieves the visible field with the matching name with offset index
   * information.
   *
   * @param name The name of the field to retrieve
   * @return The field as a variable info profile
   */
  @throws[NoSuchElementException]
  def indexedField(name: String): FieldVariableInfo =
    indexedFieldOption(name).get

  /**
   * Retrieves the visible field with the matching name with offset index
   * information.
   *
   * @param name The name of the field to retrieve
   * @return Some field as a variable info profile, or None if doesn't exist
   */
  @CannotFreeze
  def indexedFieldOption(name: String): Option[FieldVariableInfo]

  /**
   * Retrieves the visible field with the matching name with offset index
   * information.
   *
   * @param name The name of the field to retrieve
   * @return Success containing the field as a variable info profile, otherwise
   *         a failure
   */
  def tryIndexedField(name: String): Try[FieldVariableInfo] =
    Try(indexedField(name))

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return Some field as a variable info profile, or None if doesn't exist
   */
  @CannotFreeze
  def fieldOption(name: String): Option[FieldVariableInfo]

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return The field as a variable info profile
   */
  @throws[NoSuchElementException]
  def field(name: String): FieldVariableInfo = fieldOption(name).get

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return Success containing the field as a variable info profile, otherwise
   *         a failure
   */
  def tryField(name: String): Try[FieldVariableInfo] = Try(field(name))

  /**
   * Retrieves all methods declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return The collection of methods as method info profiles
   */
  @FreezeMetadata(ReturnType.FreezeCollection)
  @CanFreeze
  def allMethods: Seq[MethodInfo]

  /**
   * Retrieves all methods declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return Success containing the collection of methods as method info
   *         profiles, otherwise a failure
   */
  def tryAllMethods: Try[Seq[MethodInfo]] = Try(allMethods)

  /**
   * Retrieves unhidden and unambiguous methods in this type. Methods hidden
   * by other methods with the same name (in a more recently inherited class)
   * are not included. Methods that are ambiguously multiply inherited are also
   * not included. All other inherited methods are included.
   *
   * @return The collection of methods as method info profiles
   */
  @FreezeMetadata(ReturnType.FreezeCollection)
  @CanFreeze
  def visibleMethods: Seq[MethodInfo]

  /**
   * Retrieves unhidden and unambiguous methods in this type. Methods hidden
   * by other methods with the same name (in a more recently inherited class)
   * are not included. Methods that are ambiguously multiply inherited are also
   * not included. All other inherited methods are included.
   *
   * @return Success containing the collection of methods as method info
   *         profiles, otherwise a failure
   */
  def tryVisibleMethods: Try[Seq[MethodInfo]] = Try(visibleMethods)

  /**
   * Retrieves the visible methods with the matching name.
   *
   * @param name The name of the method to retrieve
   * @return The collection of method info profiles
   */
  @CannotFreeze
  def methods(name: String): Seq[MethodInfo]

  /**
   * Retrieves the visible method with the matching name.
   *
   * @param name The name of the method to retrieve
   * @return Success containing the method as a method info profile, otherwise
   *         a failure
   */
  def tryMethods(name: String): Try[Seq[MethodInfo]] =
    Try(methods(name))

  /**
   * Retrieves the classloader object which loaded the class associated with
   * this type.
   *
   * @return The profile representing the classloader
   */
  @throws[NoSuchElementException]
  def classLoader: ClassLoaderInfo = classLoaderOption.get

  /**
   * Retrieves the classloader object which loaded the class associated with
   * this type.
   *
   * @return Some profile representing the classloader,
   *         otherwise None if loaded through the bootstrap classloader
   */
  @FreezeMetadata(ReturnType.FreezeOption)
  @CanFreeze
  def classLoaderOption: Option[ClassLoaderInfo]

  /**
   * Retrieves the class object associated with this type.
   *
   * @return The profile representing the class
   */
  @FreezeMetadata(ReturnType.FreezeObject)
  @CanFreeze
  def classObject: ClassObjectInfo

  /**
   * Retrieves the generic signature type if it exists.
   *
   * @return Some signature if it exists, otherwise None
   */
  @CanFreeze
  def genericSignature: Option[String]

  /**
   * Retrieves reachable instances of this type.
   *
   * @param maxInstances The maximum number of instances to return, or zero
   *                     to get all reachable instances
   * @return The collection of object instances
   */
  @CannotFreeze
  def instances(maxInstances: Long): Seq[ObjectInfo]

  /**
   * Retrieves reachable instances of this type.
   *
   * @param maxInstances The maximum number of instances to return, or zero
   *                     to get all reachable instances
   * @return Success containing the collection of object instances, otherwise
   *         a failure
   */
  def tryInstances(maxInstances: Long): Try[Seq[ObjectInfo]] =
    Try(instances(maxInstances))

  /**
   * Retrieves all reachable instances of this type.
   *
   * @return The collection of object instances
   */
  def allInstances: Seq[ObjectInfo] = instances(0)

  /**
   * Retrieves all reachable instances of this type.
   *
   * @return Success containing the collection of object instances, otherwise
   *         a failure
   */
  def tryAllInstances: Try[Seq[ObjectInfo]] = Try(allInstances)

  /**
   * Indicates whether or not this type is abstract.
   *
   * @return True if abstract, otherwise false
   */
  @CanFreeze
  def isAbstract: Boolean

  /**
   * Indicates whether or not this type is final.
   *
   * @return True if final, otherwise false
   */
  @CanFreeze
  def isFinal: Boolean

  /**
   * Indicates whether or not this type has been initialized. This value is
   * the same as isPrepared for interfaces and is undefined for arrays and
   * primitive types.
   *
   * @return True if initialized, otherwise false
   */
  @CanFreeze
  def isInitialized: Boolean

  /**
   * Indicates whether or not this type's class has been prepared.
   *
   * @return True if prepared, otherwise false
   */
  @CanFreeze
  def isPrepared: Boolean

  /**
   * Indicates whether or not this type is static.
   *
   * @return True if static, otherwise false
   */
  @CanFreeze
  def isStatic: Boolean

  /**
   * Indicates whether or not this type has been verified. This value is
   * the same as isPrepared for interfaces and is undefined for arrays and
   * primitive types.
   *
   * @return True if verified, otherwise false
   */
  @CanFreeze
  def isVerified: Boolean

  /**
   * Retrieves and returns all valid locations for executable lines within
   * this type.
   *
   * @return The collection of location information
   */
  @FreezeMetadata(ReturnType.FreezeCollection)
  @CanFreeze
  def allLineLocations: Seq[LocationInfo]

  /**
   * Retrieves and returns all valid locations for executable lines within
   * this type.
   *
   * @return Success containing the collection of location information,
   *         otherwise a failure
   */
  def tryAllLineLocations: Try[Seq[LocationInfo]] =
    Try(allLineLocations)

  /**
   * Retrieves and returns all valid locations for a specific executable line
   * within this type.
   *
   * @return The collection of location information
   */
  @CannotFreeze
  def locationsOfLine(line: Int): Seq[LocationInfo]

  /**
   * Retrieves and returns all valid locations for a specific executable line
   * within this type.
   *
   * @return Success containing the collection of location information,
   *         otherwise a failure
   */
  def tryLocationsOfLine(line: Int): Try[Seq[LocationInfo]] =
    Try(locationsOfLine(line))

  /**
   * Retrieves the major class version number defined in the class file format
   * of the JVM specification.
   *
   * @return The major version number
   */
  @CanFreeze
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
  @CanFreeze
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
  @CanFreeze
  def name: String

  /**
   * Retrieves reference type information for all types declared inside this
   * type.
   *
   * @return The collection of reference type information
   */
  @FreezeMetadata(ReturnType.FreezeCollection)
  @CanFreeze
  def nestedTypes: Seq[ReferenceTypeInfo]

  /**
   * Retrieves the source debug extension for this type.
   *
   * @return The source debug extension
   */
  @CanFreeze
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
  @CanFreeze
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
  @CanFreeze
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
