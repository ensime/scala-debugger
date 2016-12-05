package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ObjectReference
import org.scaladebugger.api.lowlevel.JDIArgument

import scala.util.Try

/**
 * Represents the interface for object-based interaction.
 */
trait ObjectInfoProfile extends ValueInfoProfile with CommonInfoProfile {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: ObjectInfoProfile

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ObjectReference

  /**
   * Returns the type information for the object.
   *
   * @return The profile containing type information
   */
  override def typeInfo: ReferenceTypeInfoProfile

  /**
   * Represents the unique id of this object.
   *
   * @return The unique id as a long
   */
  def uniqueId: Long

  /**
   * Represents the unique id of this object in hex form. There is no 0x
   * prepended to the value.
   *
   * @return The raw hex string
   */
  def uniqueIdHexString: String = uniqueId.toHexString.toUpperCase()

  /**
   * Returns the reference type information for this object.
   *
   * @note Returns the specific type of this object, not any interface or
   *       superclass that it inherits. So, val x: AnyRef = "a string" would
   *       yield the reference type for String, not AnyRef.
   * @return The reference type information
   */
  def referenceType: ReferenceTypeInfoProfile

  /**
   * Invokes the object's method with matching name and arguments.
   *
   * @param thread The thread within which to invoke the method
   * @param methodName The name of the method to invoke
   * @param arguments The arguments to provide to the method
   * @param jdiArguments Optional arguments to provide custom settings to the
   *                     method invocation
   * @return Success containing the resulting value of the invocation, otherwise
   *         a failure
   */
  def tryInvoke(
    thread: ThreadInfoProfile,
    methodName: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ValueInfoProfile] = tryInvoke(
    thread,
    methodName,
    arguments.map(_.getClass.getName),
    arguments,
    jdiArguments: _*
  )

  /**
   * Invokes the object's method with matching name and arguments.
   *
   * @param thread The thread within which to invoke the method
   * @param methodName The name of the method to invoke
   * @param arguments The arguments to provide to the method
   * @param jdiArguments Optional arguments to provide custom settings to the
   *                     method invocation
   * @return The resulting value of the invocation
   */
  def invoke(
    thread: ThreadInfoProfile,
    methodName: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfoProfile = invoke(
    thread,
    methodName,
    arguments.map(_.getClass.getName),
    arguments,
    jdiArguments: _*
  )

  /**
   * Invokes the object's method with matching name and arguments.
   *
   * @param thread The thread within which to invoke the method
   * @param methodName The name of the method to invoke
   * @param parameterTypeNames The names of the parameter types of the method
   *                           to invoke
   * @param arguments The arguments to provide to the method
   * @param jdiArguments Optional arguments to provide custom settings to the
   *                     method invocation
   * @return Success containing the resulting value of the invocation, otherwise
   *         a failure
   */
  def tryInvoke(
    thread: ThreadInfoProfile,
    methodName: String,
    parameterTypeNames: Seq[String],
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ValueInfoProfile] = Try(invoke(
    thread,
    methodName,
    parameterTypeNames,
    arguments,
    jdiArguments: _*
  ))

  /**
   * Invokes the object's method with matching name and arguments.
   *
   * @param thread The thread within which to invoke the method
   * @param methodName The name of the method to invoke
   * @param parameterTypeNames The names of the parameter types of the method
   *                           to invoke
   * @param arguments The arguments to provide to the method
   * @param jdiArguments Optional arguments to provide custom settings to the
   *                     method invocation
   * @return The resulting value of the invocation
   * @throws AssertionError When the parameter type and argument counts are not
   *                        equivalent
   */
  @throws[AssertionError]
  def invoke(
    thread: ThreadInfoProfile,
    methodName: String,
    parameterTypeNames: Seq[String],
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfoProfile = {
    assert(parameterTypeNames.length == arguments.length,
      "Inconsistent number of parameter types versus arguments!")

    invoke(
      thread,
      method(methodName, parameterTypeNames: _*),
      arguments,
      jdiArguments: _*
    )
  }

  /**
   * Invokes the object's method.
   *
   * @param thread The thread within which to invoke the method
   * @param method The method of the object to invoke
   * @param arguments The arguments to provide to the method
   * @param jdiArguments Optional arguments to provide custom settings to the
   *                     method invocation
   * @return Success containing the resulting value of the invocation, otherwise
   *         a failure
   */
  def tryInvoke(
    thread: ThreadInfoProfile,
    method: MethodInfoProfile,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ValueInfoProfile] = Try(invoke(
    thread,
    method,
    arguments,
    jdiArguments: _*
  ))

  /**
   * Invokes the object's method.
   *
   * @param thread The thread within which to invoke the method
   * @param method The method of the object to invoke
   * @param arguments The arguments to provide to the method
   * @param jdiArguments Optional arguments to provide custom settings to the
   *                     method invocation
   * @return The resulting value of the invocation
   */
  def invoke(
    thread: ThreadInfoProfile,
    method: MethodInfoProfile,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfoProfile

  /**
   * Returns all visible fields contained in this object.
   *
   * @return Success containing the profiles wrapping the visible fields in
   *         this object, otherwise a failure
   */
  def tryFields: Try[Seq[FieldVariableInfoProfile]] = Try(fields)

  /**
   * Returns all visible fields contained in this object.
   *
   * @return The profiles wrapping the visible fields in this object
   */
  def fields: Seq[FieldVariableInfoProfile]

  /**
   * Returns all visible fields contained in this object with offset index.
   *
   * @return Success containing the profiles wrapping the visible fields in
   *         this object, otherwise a failure
   */
  def tryIndexedFields: Try[Seq[FieldVariableInfoProfile]] = Try(indexedFields)

  /**
   * Returns all visible fields contained in this object with offset index.
   *
   * @return The profiles wrapping the visible fields in this object
   */
  def indexedFields: Seq[FieldVariableInfoProfile]

  /**
   * Returns the object's field with the specified name with offset index
   * information.
   *
   * @param name The name of the field
   * @return Success containing the profile wrapping the field, otherwise
   *         a failure
   */
  def tryIndexedField(name: String): Try[FieldVariableInfoProfile] =
    Try(indexedField(name))

  /**
   * Returns the object's field with the specified name with offset index
   * information.
   *
   * @param name The name of the field
   * @return The profile wrapping the field
   */
  @throws[NoSuchElementException]
  def indexedField(name: String): FieldVariableInfoProfile =
    indexedFieldOption(name).get

  /**
   * Returns the object's field with the specified name with offset index
   * information.
   *
   * @param name The name of the field
   * @return Some profile wrapping the field, or None if doesn't exist
   */
  def indexedFieldOption(name: String): Option[FieldVariableInfoProfile]

  /**
   * Returns the object's field with the specified name.
   *
   * @param name The name of the field
   * @return Success containing the profile wrapping the field, otherwise
   *         a failure
   */
  def tryField(name: String): Try[FieldVariableInfoProfile] = Try(field(name))

  /**
   * Returns the object's field with the specified name.
   *
   * @param name The name of the field
   * @return The profile wrapping the field
   */
  @throws[NoSuchElementException]
  def field(name: String): FieldVariableInfoProfile = fieldOption(name).get

  /**
   * Returns the object's field with the specified name.
   *
   * @param name The name of the field
   * @return Some profile wrapping the field, or None if doesn't exist
   */
  def fieldOption(name: String): Option[FieldVariableInfoProfile]

  /**
   * Returns all visible methods contained in this object.
   *
   * @return Success containing the profiles wrapping the visible methods in
   *         this object, otherwise a failure
   */
  def tryMethods: Try[Seq[MethodInfoProfile]] = Try(methods)

  /**
   * Returns all visible methods contained in this object.
   *
   * @return The profiles wrapping the visible methods in this object
   */
  def methods: Seq[MethodInfoProfile]

  /**
   * Returns the object's method with the specified name.
   *
   * @param name The name of the method
   * @param parameterTypeNames The fully-qualified type names of the parameters
   *                           of the method to find
   * @return Success containing the profile wrapping the method, otherwise
   *         a failure
   */
  def tryMethod(
    name: String,
    parameterTypeNames: String*
  ): Try[MethodInfoProfile] = Try(method(name, parameterTypeNames: _*))

  /**
   * Returns the object's method with the specified name.
   *
   * @param name The name of the method
   * @param parameterTypeNames The fully-qualified type names of the parameters
   *                           of the method to find
   * @return The profile wrapping the method
   */
  @throws[NoSuchElementException]
  def method(
    name: String,
    parameterTypeNames: String*
  ): MethodInfoProfile = methodOption(name, parameterTypeNames: _*).get

  /**
   * Returns the object's method with the specified name.
   *
   * @param name The name of the method
   * @param parameterTypeNames The fully-qualified type names of the parameters
   *                           of the method to find
   * @return Some profile wrapping the method, otherwise None if doesn't exist
   */
  def methodOption(
    name: String,
    parameterTypeNames: String*
  ): Option[MethodInfoProfile]

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    val typeName = this.referenceType.name
    val uniqueHexCode = this.uniqueId.toHexString.toUpperCase()
    s"Instance of $typeName (0x$uniqueHexCode)"
  }
}
