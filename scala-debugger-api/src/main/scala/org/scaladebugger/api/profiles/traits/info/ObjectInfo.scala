package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ObjectReference
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
import org.scaladebugger.macros.freeze.{CanFreeze, CannotFreeze, Freezable}

import scala.util.Try

/**
 * Represents the interface for object-based interaction.
 */
//@Freezable
trait ObjectInfo extends ValueInfo with CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  @CannotFreeze
  override def toJavaInfo: ObjectInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  @CannotFreeze
  override def toJdiInstance: ObjectReference

  /**
   * Returns the type information for the object.
   *
   * @return The profile containing type information
   */
  @CanFreeze(ReturnType.FreezeObject)
  override def `type`: ReferenceTypeInfo

  /**
   * Represents the unique id of this object.
   *
   * @return The unique id as a long
   */
  @CanFreeze
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
  @CanFreeze(ReturnType.FreezeObject)
  def referenceType: ReferenceTypeInfo

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
    thread: ThreadInfo,
    methodName: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ValueInfo] = tryInvoke(
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
    thread: ThreadInfo,
    methodName: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfo = invoke(
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
    thread: ThreadInfo,
    methodName: String,
    parameterTypeNames: Seq[String],
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ValueInfo] = Try(invoke(
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
    thread: ThreadInfo,
    methodName: String,
    parameterTypeNames: Seq[String],
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfo = {
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
    thread: ThreadInfo,
    method: MethodInfo,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ValueInfo] = Try(invoke(
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
  @CannotFreeze
  def invoke(
    thread: ThreadInfo,
    method: MethodInfo,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfo

  /**
   * Returns all visible fields contained in this object.
   *
   * @return Success containing the profiles wrapping the visible fields in
   *         this object, otherwise a failure
   */
  def tryFields: Try[Seq[FieldVariableInfo]] = Try(fields)

  /**
   * Returns all visible fields contained in this object.
   *
   * @return The profiles wrapping the visible fields in this object
   */
  @CanFreeze(ReturnType.FreezeCollection)
  def fields: Seq[FieldVariableInfo]

  /**
   * Returns all visible fields contained in this object with offset index.
   *
   * @return Success containing the profiles wrapping the visible fields in
   *         this object, otherwise a failure
   */
  def tryIndexedFields: Try[Seq[FieldVariableInfo]] = Try(indexedFields)

  /**
   * Returns all visible fields contained in this object with offset index.
   *
   * @return The profiles wrapping the visible fields in this object
   */
  @CanFreeze(ReturnType.FreezeCollection)
  def indexedFields: Seq[FieldVariableInfo]

  /**
   * Returns the object's field with the specified name with offset index
   * information.
   *
   * @param name The name of the field
   * @return Success containing the profile wrapping the field, otherwise
   *         a failure
   */
  def tryIndexedField(name: String): Try[FieldVariableInfo] =
    Try(indexedField(name))

  /**
   * Returns the object's field with the specified name with offset index
   * information.
   *
   * @param name The name of the field
   * @return The profile wrapping the field
   */
  @throws[NoSuchElementException]
  def indexedField(name: String): FieldVariableInfo =
    indexedFieldOption(name).get

  /**
   * Returns the object's field with the specified name with offset index
   * information.
   *
   * @param name The name of the field
   * @return Some profile wrapping the field, or None if doesn't exist
   */
  @CannotFreeze
  def indexedFieldOption(name: String): Option[FieldVariableInfo]

  /**
   * Returns the object's field with the specified name.
   *
   * @param name The name of the field
   * @return Success containing the profile wrapping the field, otherwise
   *         a failure
   */
  def tryField(name: String): Try[FieldVariableInfo] = Try(field(name))

  /**
   * Returns the object's field with the specified name.
   *
   * @param name The name of the field
   * @return The profile wrapping the field
   */
  @throws[NoSuchElementException]
  def field(name: String): FieldVariableInfo = fieldOption(name).get

  /**
   * Returns the object's field with the specified name.
   *
   * @param name The name of the field
   * @return Some profile wrapping the field, or None if doesn't exist
   */
  @CannotFreeze
  def fieldOption(name: String): Option[FieldVariableInfo]

  /**
   * Returns all visible methods contained in this object.
   *
   * @return Success containing the profiles wrapping the visible methods in
   *         this object, otherwise a failure
   */
  def tryMethods: Try[Seq[MethodInfo]] = Try(methods)

  /**
   * Returns all visible methods contained in this object.
   *
   * @return The profiles wrapping the visible methods in this object
   */
  @CanFreeze(ReturnType.FreezeCollection)
  def methods: Seq[MethodInfo]

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
  ): Try[MethodInfo] = Try(method(name, parameterTypeNames: _*))

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
  ): MethodInfo = methodOption(name, parameterTypeNames: _*).get

  /**
   * Returns the object's method with the specified name.
   *
   * @param name The name of the method
   * @param parameterTypeNames The fully-qualified type names of the parameters
   *                           of the method to find
   * @return Some profile wrapping the method, otherwise None if doesn't exist
   */
  @CannotFreeze
  def methodOption(
    name: String,
    parameterTypeNames: String*
  ): Option[MethodInfo]

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
