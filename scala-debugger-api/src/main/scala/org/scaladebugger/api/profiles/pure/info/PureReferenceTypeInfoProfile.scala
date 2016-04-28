package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ClassLoaderInfoProfile, ClassObjectInfoProfile, _}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a reference type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            reference type
 * @param _referenceType The reference to the underlying JDI reference type
 */
class PureReferenceTypeInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _referenceType: ReferenceType
) extends PureTypeInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  _type = _referenceType
) with ReferenceTypeInfoProfile {
  private lazy val defaultStratum: String = _referenceType.defaultStratum()

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ReferenceType = _referenceType

  /**
   * Retrieves the fully-qualified class name of this type.
   *
   * @return The fully-qualified class name
   */
  override def name: String = _referenceType.name()

  /**
   * Represents the JNI-style signature for this type. Primitives have the
   * signature of their corresponding class representation such as "I" for
   * Integer.TYPE.
   *
   * @return The JNI-style signature
   */
  override def signature: String = _referenceType.signature()

  /**
   * Retrieves the generic signature type if it exists.
   *
   * @return Some signature if it exists, otherwise None
   */
  override def genericSignature: Option[String] =
    Option(_referenceType.genericSignature())

  /**
   * Retrieves the source debug extension for this type.
   *
   * @return The source debug extension
   */
  override def sourceDebugExtension: String =
    _referenceType.sourceDebugExtension()

  /**
   * Retrieves all identifying names for the source(s) corresponding to this
   * type.
   *
   * @return The collection of identifying names
   */
  override def sourceNames: Seq[String] = {
    import scala.collection.JavaConverters._
    _referenceType.sourceNames(defaultStratum).asScala
  }

  /**
   * Retrieves all source paths corresponding to this type.
   *
   * @return The collection of source paths
   */
  override def sourcePaths: Seq[String] = {
    import scala.collection.JavaConverters._
    _referenceType.sourcePaths(defaultStratum).asScala
  }

  /**
   * Retrieves all fields declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @note Provides no offset index information!
   * @return The collection of fields as variable info profiles
   */
  override def allFields: Seq[VariableInfoProfile] = {
    import scala.collection.JavaConverters._
    _referenceType.allFields().asScala.map(newFieldProfile)
  }

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included.
   *
   * @note Provides offset index information!
   * @return The collection of fields as variable info profiles
   */
  override def visibleFields: Seq[VariableInfoProfile] = {
    import scala.collection.JavaConverters._
    _referenceType.visibleFields().asScala.map(newFieldProfile)
  }

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return Some field as a variable info profile, or None if doesn't exist
   */
  override def fieldOption(name: String): Option[VariableInfoProfile] = {
    Option(_referenceType.fieldByName(name)).map(newFieldProfile)
  }

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included. Offset index
   * information is included.
   *
   * @return The collection of fields as variable info profiles
   */
  override def indexedVisibleFields: Seq[VariableInfoProfile] = {
    import scala.collection.JavaConverters._
    _referenceType.visibleFields().asScala.zipWithIndex.map { case (f, i) =>
      newFieldProfile(f, i)
    }
  }

  /**
   * Retrieves the visible field with the matching name with offset index
   * information.
   *
   * @param name The name of the field to retrieve
   * @return Some field as a variable info profile, or None if doesn't exist
   */
  override def indexedFieldOption(name: String): Option[VariableInfoProfile] = {
    indexedVisibleFields.reverse.find(_.name == name)
  }

  /**
   * Retrieves all methods declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return The collection of methods as method info profiles
   */
  override def allMethods: Seq[MethodInfoProfile] = {
    import scala.collection.JavaConverters._
    _referenceType.allMethods().asScala.map(newMethodProfile)
  }

  /**
   * Retrieves unhidden and unambiguous methods in this type. Methods hidden
   * by other methods with the same name (in a more recently inherited class)
   * are not included. Methods that are ambiguously multiply inherited are also
   * not included. All other inherited methods are included.
   *
   * @return The collection of methods as method info profiles
   */
  override def visibleMethods: Seq[MethodInfoProfile] = {
    import scala.collection.JavaConverters._
    _referenceType.visibleMethods().asScala.map(newMethodProfile)
  }

  /**
   * Retrieves the visible methods with the matching name.
   *
   * @param name The name of the method to retrieve
   * @return The collection of method info profiles
   */
  override def methods(name: String): Seq[MethodInfoProfile] = {
    import scala.collection.JavaConverters._
    _referenceType.methodsByName(name).asScala.map(newMethodProfile)
  }

  /**
   * Retrieves and returns all valid locations for a specific executable line
   * within this type.
   *
   * @return The collection of location information
   */
  override def locationsOfLine(line: Int): Seq[LocationInfoProfile] = {
    import scala.collection.JavaConverters._
    _referenceType.locationsOfLine(line).asScala.map(newLocationProfile)
  }

  /**
   * Retrieves and returns all valid locations for executable lines within
   * this type.
   *
   * @return The collection of location information
   */
  override def allLineLocations: Seq[LocationInfoProfile] = {
    import scala.collection.JavaConverters._
    _referenceType.allLineLocations().asScala.map(newLocationProfile)
  }

  /**
   * Retrieves reference type information for all types declared inside this
   * tupe.
   *
   * @return The collection of reference type information
   */
  override def nestedTypes: Seq[ReferenceTypeInfoProfile] = {
    import scala.collection.JavaConverters._
    _referenceType.nestedTypes().asScala.map(newReferenceTypeProfile)
  }

  /**
   * Retrieves reachable instances of this type.
   *
   * @param maxInstances The maximum number of instances to return, or zero
   *                     to get all reachable instances
   * @return The collection of object instances
   */
  override def instances(maxInstances: Long): Seq[ObjectInfoProfile] = {
    require(maxInstances >= 0, "Max instances cannot be negative!")

    import scala.collection.JavaConverters._
    _referenceType.instances(maxInstances).asScala.map(newObjectProfile)
  }

  /**
   * Retrieves the classloader object which loaded the class associated with
   * this type.
   *
   * @return Some profile representing the classloader,
   *         otherwise None if loaded through the bootstrap classloader
   */
  override def classLoaderOption: Option[ClassLoaderInfoProfile] =
    Option(_referenceType.classLoader()).map(newClassLoaderProfile)

  /**
   * Retrieves the class object associated with this type.
   *
   * @return The profile representing the class
   */
  override def classObject: ClassObjectInfoProfile = newClassObjectProfile(
    _referenceType.classObject()
  )

  /**
   * Indicates whether or not this type is final.
   *
   * @return True if final, otherwise false
   */
  override def isFinal: Boolean = _referenceType.isFinal

  /**
   * Indicates whether or not this type's class has been prepared.
   *
   * @return True if prepared, otherwise false
   */
  override def isPrepared: Boolean = _referenceType.isPrepared

  /**
   * Indicates whether or not this type has been initialized. This value is
   * the same as isPrepared for interfaces and is undefined for arrays and
   * primitive types.
   *
   * @return True if initialized, otherwise false
   */
  override def isInitialized: Boolean = _referenceType.isInitialized

  /**
   * Indicates whether or not this type is static.
   *
   * @return True if static, otherwise false
   */
  override def isStatic: Boolean = _referenceType.isStatic

  /**
   * Indicates whether or not this type is abstract.
   *
   * @return True if abstract, otherwise false
   */
  override def isAbstract: Boolean = _referenceType.isAbstract

  /**
   * Indicates whether or not this type has been verified. This value is
   * the same as isPrepared for interfaces and is undefined for arrays and
   * primitive types.
   *
   * @return True if verified, otherwise false
   */
  override def isVerified: Boolean = _referenceType.isVerified

  /**
   * Retrieves the major class version number defined in the class file format
   * of the JVM specification.
   *
   * @return The major version number
   */
  override def majorVersion: Int = _referenceType.majorVersion()

  /**
   * Retrieves the minor class version number defined in the class file format
   * of the JVM specification.
   *
   * @return The minor version number
   */
  override def minorVersion: Int = _referenceType.minorVersion()

  protected def newFieldProfile(field: Field): VariableInfoProfile =
    newFieldProfile(field, -1)

  protected def newFieldProfile(
    field: Field,
    offsetIndex: Int
  ): VariableInfoProfile = new PureFieldInfoProfile(
    scalaVirtualMachine,
    Right(_referenceType),
    field,
    offsetIndex
  )()

  protected def newMethodProfile(method: Method): MethodInfoProfile =
    new PureMethodInfoProfile(scalaVirtualMachine, method)

  protected def newObjectProfile(objectReference: ObjectReference): ObjectInfoProfile =
    new PureObjectInfoProfile(scalaVirtualMachine, objectReference)()

  protected def newLocationProfile(location: Location): LocationInfoProfile =
    new PureLocationInfoProfile(scalaVirtualMachine, location)

  protected def newClassObjectProfile(
    classObjectReference: ClassObjectReference
  ): ClassObjectInfoProfile = new PureClassObjectInfoProfile(
    scalaVirtualMachine,
    classObjectReference
  )(
    _referenceType = _referenceType
  )

  protected def newClassLoaderProfile(
    classLoaderReference: ClassLoaderReference
  ): ClassLoaderInfoProfile = new PureClassLoaderInfoProfile(
    scalaVirtualMachine,
    classLoaderReference
  )(
    _referenceType = _referenceType
  )
}
