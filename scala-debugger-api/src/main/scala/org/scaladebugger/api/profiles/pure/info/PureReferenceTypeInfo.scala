package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ClassLoaderInfo, ClassObjectInfo, _}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a reference type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            reference type
 * @param infoProducer The producer of info-based profile instances
 * @param _referenceType The reference to the underlying JDI reference type
 */
class PureReferenceTypeInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val _referenceType: ReferenceType
) extends PureTypeInfo(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  _type = _referenceType
) with ReferenceTypeInfo {
  private lazy val defaultStratum: String = _referenceType.defaultStratum()

  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = true

  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: ReferenceTypeInfo = {
    infoProducer.toJavaInfo.newReferenceTypeInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      referenceType = _referenceType
    )
  }

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
  override def allFields: Seq[FieldVariableInfo] = {
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
  override def visibleFields: Seq[FieldVariableInfo] = {
    import scala.collection.JavaConverters._
    _referenceType.visibleFields().asScala.map(newFieldProfile)
  }

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return Some field as a variable info profile, or None if doesn't exist
   */
  override def fieldOption(name: String): Option[FieldVariableInfo] = {
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
  override def indexedVisibleFields: Seq[FieldVariableInfo] = {
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
  override def indexedFieldOption(
    name: String
  ): Option[FieldVariableInfo] = {
    indexedVisibleFields.reverse.find(_.name == name)
  }

  /**
   * Retrieves all methods declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return The collection of methods as method info profiles
   */
  override def allMethods: Seq[MethodInfo] = {
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
  override def visibleMethods: Seq[MethodInfo] = {
    import scala.collection.JavaConverters._
    _referenceType.visibleMethods().asScala.map(newMethodProfile)
  }

  /**
   * Retrieves the visible methods with the matching name.
   *
   * @param name The name of the method to retrieve
   * @return The collection of method info profiles
   */
  override def methods(name: String): Seq[MethodInfo] = {
    import scala.collection.JavaConverters._
    _referenceType.methodsByName(name).asScala.map(newMethodProfile)
  }

  /**
   * Retrieves and returns all valid locations for a specific executable line
   * within this type.
   *
   * @return The collection of location information
   */
  override def locationsOfLine(line: Int): Seq[LocationInfo] = {
    import scala.collection.JavaConverters._
    _referenceType.locationsOfLine(line).asScala.map(newLocationProfile)
  }

  /**
   * Retrieves and returns all valid locations for executable lines within
   * this type.
   *
   * @return The collection of location information
   */
  override def allLineLocations: Seq[LocationInfo] = {
    import scala.collection.JavaConverters._
    _referenceType.allLineLocations().asScala.map(newLocationProfile)
  }

  /**
   * Retrieves reference type information for all types declared inside this
   * tupe.
   *
   * @return The collection of reference type information
   */
  override def nestedTypes: Seq[ReferenceTypeInfo] = {
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
  override def instances(maxInstances: Long): Seq[ObjectInfo] = {
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
  override def classLoaderOption: Option[ClassLoaderInfo] =
    Option(_referenceType.classLoader()).map(newClassLoaderProfile)

  /**
   * Retrieves the class object associated with this type.
   *
   * @return The profile representing the class
   */
  override def classObject: ClassObjectInfo = newClassObjectProfile(
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

  protected def newFieldProfile(field: Field): FieldVariableInfo =
    newFieldProfile(field, -1)

  protected def newFieldProfile(
    field: Field,
    offsetIndex: Int
  ): FieldVariableInfo = infoProducer.newFieldInfoProfile(
    scalaVirtualMachine,
    Right(_referenceType),
    field,
    offsetIndex
  )()

  protected def newMethodProfile(method: Method): MethodInfo =
    infoProducer.newMethodInfoProfile(scalaVirtualMachine, method)

  protected def newObjectProfile(objectReference: ObjectReference): ObjectInfo =
    infoProducer.newObjectInfoProfile(scalaVirtualMachine, objectReference)()

  protected def newLocationProfile(location: Location): LocationInfo =
    infoProducer.newLocationInfoProfile(scalaVirtualMachine, location)

  protected def newClassObjectProfile(
    classObjectReference: ClassObjectReference
  ): ClassObjectInfo = infoProducer.newClassObjectInfoProfile(
    scalaVirtualMachine,
    classObjectReference
  )(referenceType = _referenceType)

  protected def newClassLoaderProfile(
    classLoaderReference: ClassLoaderReference
  ): ClassLoaderInfo = infoProducer.newClassLoaderInfoProfile(
    scalaVirtualMachine,
    classLoaderReference
  )(referenceType = _referenceType)
}
