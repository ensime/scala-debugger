package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.Method

import scala.util.Try

/**
 * Represents the interface for method-based interaction.
 */
trait MethodInfo extends CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: MethodInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Method

  /**
   * Returns the name of this method.
   *
   * @return The name of the method
   */
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
  def parameterTypeNames: Seq[String]

  /**
   * Returns the type information for the method's parameter types.
   *
   * @return The collection of profiles containing type information
   */
  def parameterTypeInfo: Seq[TypeInfo]

  /**
   * Returns the type information for the method's parameter types.
   *
   * @return Success containing the collection of profiles containing type
   *         information
   */
  def tryParameterTypeInfo: Try[Seq[TypeInfo]] = Try(parameterTypeInfo)

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
  def returnTypeName: String

  /**
   * Returns the type information for the method's return type.
   *
   * @return The profile containing type information
   */
  def returnTypeInfo: TypeInfo

  /**
   * Returns the type information for the method's return type.
   *
   * @return The profile containing type information
   */
  def tryReturnTypeInfo: Try[TypeInfo] = Try(returnTypeInfo)

  /**
   * Returns the type where this method was declared.
   *
   * @return The reference type information that declared this method
   */
  def declaringTypeInfo: ReferenceTypeInfo

  /**
   * Returns the type where this method was declared.
   *
   * @return The reference type information that declared this method
   */
  def tryDeclaringTypeInfo: Try[ReferenceTypeInfo] =
    Try(declaringTypeInfo)

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
