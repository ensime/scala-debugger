package org.scaladebugger.api.profiles.traits.info

import acyclic.file
import com.sun.jdi.ClassType
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
import org.scaladebugger.macros.freeze.{CanFreeze, CannotFreeze, Freezable}

import scala.util.Try

/**
 * Represents the interface for retrieving class type-based information.
 */
//@Freezable
trait ClassTypeInfo extends ReferenceTypeInfo with TypeInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  @CannotFreeze
  override def toJavaInfo: ClassTypeInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  @CannotFreeze
  override def toJdiInstance: ClassType

  /**
   * Returns a collection of type information for interfaces directly and
   * indirectly implemented by this class.
   *
   * @return The collection of interface type info profiles
   */
  @CanFreeze(ReturnType.FreezeCollection)
  def allInterfaces: Seq[InterfaceTypeInfo]

  /**
   * Returns a collection of type information for interfaces directly and
   * indirectly implemented by this class.
   *
   * @return Success containing the collection of interface type info profiles,
   *         otherwise a failure
   */
  def tryAllInterfaces: Try[Seq[InterfaceTypeInfo]] = Try(allInterfaces)

  /**
   * Returns a collection of interfaces directly implemented by this class.
   *
   * @return The collection of interface type info profiles
   */
  @CanFreeze(ReturnType.FreezeCollection)
  def interfaces: Seq[InterfaceTypeInfo]

  /**
   * Returns a collection of interfaces directly implemented by this class.
   *
   * @return Success containing the collection of interface type info profiles,
   *         otherwise a failure
   */
  def tryInterfaces: Try[Seq[InterfaceTypeInfo]] = Try(interfaces)

  /**
   * Returns the loaded, direct subclasses of this class.
   *
   * @return The collection of class type info profiles
   */
  @CanFreeze(ReturnType.FreezeCollection)
  def subclasses: Seq[ClassTypeInfo]

  /**
   * Returns the superclass of this class.
   *
   * @return Some class type info if the super class exists, otherwise None
   */
  @CanFreeze(ReturnType.FreezeOption)
  def superclassOption: Option[ClassTypeInfo]

  /**
   * Indicates whether or not this class is a Java enumeration.
   *
   * @return True if it is an enumeration, otherwise false
   */
  @CanFreeze
  def isEnumeration: Boolean

  /**
   * Retrieves the single, non-abstract, visible method on this class with the
   * matching name and JNI signature.
   *
   * @param name The name of the method
   * @param signature The JNI signature of the method
   * @return Some method if found, otherwise None
   */
  @CannotFreeze
  def methodOption(name: String, signature: String): Option[MethodInfo]

  /**
   * Retrieves the single, non-abstract, visible method on this class with the
   * matching name and JNI signature.
   *
   * @param name The name of the method
   * @param signature The JNI signature of the method
   * @return Success containing the method if found, otherwise a failure
   */
  def tryMethod(name: String, signature: String): Try[MethodInfo] =
    Try(methodOption(name, signature).get)

  /**
   * Invokes the class' static method.
   *
   * @param thread The thread within which to invoke the method
   * @param method The method of the class to invoke
   * @param arguments         The arguments to provide to the method
   * @param jdiArguments      Optional arguments to provide custom settings to
   *                          the method invocation
   * @return The resulting value of the invocation
   */
  def tryInvokeStaticMethod(
    thread: ThreadInfo,
    method: MethodInfo,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ValueInfo] = Try(invokeStaticMethod(
    thread,
    method,
    arguments,
    jdiArguments: _*
  ))

  /**
   * Invokes the class' static method.
   *
   * @param thread The thread within which to invoke the method
   * @param method The method of the class to invoke
   * @param arguments         The arguments to provide to the method
   * @param jdiArguments      Optional arguments to provide custom settings to
   *                          the method invocation
   * @return The resulting value of the invocation
   */
  @CannotFreeze
  def invokeStaticMethod(
    thread: ThreadInfo,
    method: MethodInfo,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfo

  /**
   * Invokes the class' static method with matching name and arguments.
   *
   * @param thread The thread within which to invoke the method
   * @param methodName The name of the method to invoke
   * @param methodSignature The signature of the method to invoke
   * @param arguments The arguments to provide to the method
   * @param jdiArguments Optional arguments to provide custom settings to the
   *                     method invocation
   * @return Success containing the resulting value of the invocation, otherwise
   *         a failure
   */
  def tryInvokeStaticMethod(
    thread: ThreadInfo,
    methodName: String,
    methodSignature: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ValueInfo] = Try(invokeStaticMethod(
    thread,
    methodName,
    methodSignature,
    arguments,
    jdiArguments: _*
  ))

  /**
   * Invokes the class' static method with matching name and arguments.
   *
   * @param thread The thread within which to invoke the method
   * @param methodName The name of the method to invoke
   * @param methodSignature The signature of the method to invoke
   * @param arguments The arguments to provide to the method
   * @param jdiArguments Optional arguments to provide custom settings to the
   *                     method invocation
   * @return The resulting value of the invocation
   */
  @CannotFreeze
  def invokeStaticMethod(
    thread: ThreadInfo,
    methodName: String,
    methodSignature: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfo

  /**
   * Invokes the class' static method with matching name and arguments.
   *
   * @param thread The thread within which to invoke the method
   * @param constructorName The name of the constructor to invoke
   * @param constructorSignature The signature of the constructor to invoke
   * @param arguments The arguments to provide to the constructor
   * @param jdiArguments Optional arguments to provide custom settings to the
   *                     constructor invocation
   * @return Success containing the resulting value of the invocation, otherwise
   *         a failure
   */
  def tryNewInstance(
    thread: ThreadInfo,
    constructorName: String,
    constructorSignature: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ObjectInfo] = Try(newInstance(
    thread,
    constructorName,
    constructorSignature,
    arguments,
    jdiArguments: _*
  ))

  /**
   * Creates a new instance of the class.
   *
   * @param thread The thread within which to create the new instance
   * @param constructorName The name of the constructor to invoke
   * @param constructorSignature The signature of the constructor to invoke
   * @param arguments         The arguments to provide to the constructor
   * @param jdiArguments      Optional arguments to provide custom settings to
   *                          the constructor invocation
   * @return The instantiated object
   */
  @CannotFreeze
  def newInstance(
    thread: ThreadInfo,
    constructorName: String,
    constructorSignature: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ObjectInfo

  /**
   * Creates a new instance of the class.
   *
   * @param thread The thread within which to create the new instance
   * @param constructor The constructor method of the class to invoke
   * @param arguments         The arguments to provide to the constructor
   * @param jdiArguments      Optional arguments to provide custom settings to
   *                          the constructor invocation
   * @return The instantiated object
   */
  @CannotFreeze
  def newInstance(
    thread: ThreadInfo,
    constructor: MethodInfo,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ObjectInfo

  /**
   * Creates a new instance of the class.
   *
   * @param thread The thread within which to create the new instance
   * @param constructor The constructor method of the class to invoke
   * @param arguments         The arguments to provide to the constructor
   * @param jdiArguments      Optional arguments to provide custom settings to
   *                          the constructor invocation
   * @return Success containing the instantiated object, otherwise a failure
   */
  def tryNewInstance(
    thread: ThreadInfo,
    constructor: MethodInfo,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ObjectInfo] = Try(newInstance(
    thread,
    constructor,
    arguments,
    jdiArguments: _*
  ))
}
