package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.{Method, ReferenceType, Type}
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, MethodInfoProfile, ReferenceTypeInfoProfile, TypeInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a method profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            method
 * @param infoProducer The producer of info-based profile instances
 * @param _method The reference to the underlying JDI method
 */
class PureMethodInfoProfile(
  val scalaVirtualMachine: ScalaVirtualMachine,
  protected val infoProducer: InfoProducerProfile,
  private val _method: Method
) extends MethodInfoProfile {
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
  override def toJavaInfo: MethodInfoProfile = {
    infoProducer.toJavaInfo.newMethodInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      method = _method
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Method = _method

  /**
   * Returns the name of this method.
   *
   * @return The name of the method
   */
  override def name: String = _method.name

  /**
   * Returns the type information for the method's parameter types.
   *
   * @return The collection of profiles containing type information
   */
  override def parameterTypeInfo: Seq[TypeInfoProfile] = {
    import scala.collection.JavaConverters._

    _method.argumentTypes().asScala.map(newTypeProfile)
  }

  /**
   * Returns the type information for the method's return type.
   *
   * @return The profile containing type information
   */
  override def returnTypeInfo: TypeInfoProfile =
    newTypeProfile(_method.returnType())

  /**
   * Returns the fully-qualified class name of the type for the return value
   * of this method.
   *
   * @return The return type name
   */
  override def returnTypeName: String = _method.returnTypeName()


  /**
   * Returns the type where this method was declared.
   *
   * @return The reference type information that declared this method
   */
  override def declaringTypeInfo: ReferenceTypeInfoProfile =
    newReferenceTypeProfile(_method.declaringType())

  /**
   * Returns the fully-qualified class names of the types for the parameters
   * of this method.
   *
   * @return The collection of parameter type names
   */
  override def parameterTypeNames: Seq[String] = {
    import scala.collection.JavaConverters._
    _method.argumentTypeNames().asScala
  }

  protected def newReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = infoProducer.newReferenceTypeInfoProfile(
    scalaVirtualMachine,
    referenceType
  )

  protected def newTypeProfile(_type: Type): TypeInfoProfile =
    infoProducer.newTypeInfoProfile(scalaVirtualMachine, _type)
}
