package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi.ReferenceType
import org.scaladebugger.api.profiles.pure.info.PureReferenceTypeInfo
import org.scaladebugger.api.profiles.traits.info.{FieldVariableInfo, InfoProducer}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a Scala 2.10 implementation of a reference type profile.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            reference type
 * @param infoProducer The producer of info-based profile instances
 * @param _referenceType The reference to the underlying JDI reference type
 */
class Scala210ReferenceTypeInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val _referenceType: ReferenceType
) extends PureReferenceTypeInfo(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  _referenceType = _referenceType
) with Scala210FieldTransformationRules {
  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = false

  /**
    * Retrieves all fields declared in this type, its superclasses, implemented
    * interfaces, and superinterfaces.
    *
    * @note Provides no offset index information!
    * @return The collection of fields as variable info profiles
    */
  override def allFields: Seq[FieldVariableInfo] =
    super.allFields.flatMap(transformField(_, isInStaticContext = true))

   /**
    * Retrieves unhidden and unambiguous fields in this type. Fields hidden
    * by other fields with the same name (in a more recently inherited class)
    * are not included. Fields that are ambiguously multiply inherited are also
    * not included. All other inherited fields are included.
    *
    * @note Provides offset index information!
    * @return The collection of fields as variable info profiles
    */
  override def visibleFields: Seq[FieldVariableInfo] =
    super.visibleFields.flatMap(transformField(_, isInStaticContext = true))

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return Some field as a variable info profile, or None if doesn't exist
   */
  override def fieldOption(name: String): Option[FieldVariableInfo] =
    visibleFields.find(_.name == name)

  /**
    * Retrieves unhidden and unambiguous fields in this type. Fields hidden
    * by other fields with the same name (in a more recently inherited class)
    * are not included. Fields that are ambiguously multiply inherited are also
    * not included. All other inherited fields are included. Offset index
    * information is included.
    *
    * @return The collection of fields as variable info profiles
    */
  override def indexedVisibleFields: Seq[FieldVariableInfo] = visibleFields.zipWithIndex.map {
    case (f, i) => newFieldProfile(f.toJdiInstance, i)
  }

  /** Retrieves the visible field with the matching name with offset index
   * information.
   *
   * @param name The name of the field to retrieve
   * @return Some field as a variable info profile, or None if doesn't exist
   */
  override def indexedFieldOption(name: String): Option[FieldVariableInfo] =
    indexedVisibleFields.find(_.name == name)
}
