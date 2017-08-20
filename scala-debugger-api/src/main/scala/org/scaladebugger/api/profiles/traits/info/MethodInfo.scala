package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.Method
import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
import org.scaladebugger.macros.freeze.{CanFreeze, CannotFreeze, Freezable}

import scala.util.Try

/**
 * Represents the interface for method-based interaction.
 */
//@Freezable
trait MethodInfo extends CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  @CannotFreeze
  override def toJavaInfo: MethodInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  @CannotFreeze
  override def toJdiInstance: Method

  /**
   * Returns the name of this method.
   *
   * @return The name of the method
   */
  @CanFreeze
  def name: String

  /**
   * Returns the fully-qualified class names of the types for the parameters
   * of this method.
   *
   * @return Success containing the collection of parameter type names,
   *         otherwise a failure
   */
  def tryParameterTypeNames: Try[Seq[String]] = Try(parameterTypeNames)

  /**
   * Returns the fully-qualified class names of the types for the parameters
   * of this method.
   *
   * @return The collection of parameter type names
   */
  @CanFreeze
  def parameterTypeNames: Seq[String]

  /**
   * Returns the type information for the method's parameter types.
   *
   * @return The collection of profiles containing type information
   */
  @CanFreeze(ReturnType.FreezeCollection)
  def parameterTypes: Seq[TypeInfo]

  /**
   * Returns the type information for the method's parameter types.
   *
   * @return Success containing the collection of profiles containing type
   *         information
   */
  def tryParameterTypes: Try[Seq[TypeInfo]] = Try(parameterTypes)

  /**
   * Returns the fully-qualified class name of the type for the return value
   * of this method.
   *
   * @return Success containing the return type name, otherwise a failure
   */
  def tryReturnTypeName: Try[String] = Try(returnTypeName)

  /**
   * Returns the fully-qualified class name of the type for the return value
   * of this method.
   *
   * @return The return type name
   */
  @CanFreeze
  def returnTypeName: String

  /**
   * Returns the type information for the method's return type.
   *
   * @return The profile containing type information
   */
  @CanFreeze(ReturnType.FreezeObject)
  def returnType: TypeInfo

  /**
   * Returns the type information for the method's return type.
   *
   * @return The profile containing type information
   */
  def tryReturnType: Try[TypeInfo] = Try(returnType)

  /**
   * Returns the type where this method was declared.
   *
   * @return The reference type information that declared this method
   */
  @CanFreeze(ReturnType.FreezeObject)
  def declaringType: ReferenceTypeInfo

  /**
   * Returns the type where this method was declared.
   *
   * @return The reference type information that declared this method
   */
  def tryDeclaringType: Try[ReferenceTypeInfo] = Try(declaringType)

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    val params = this.tryParameterTypeNames
      .map(_.mkString(","))
      .getOrElse("???")

    val returnType = this.tryReturnTypeName.getOrElse("???")

    s"def ${this.name}($params): $returnType"
  }
}
