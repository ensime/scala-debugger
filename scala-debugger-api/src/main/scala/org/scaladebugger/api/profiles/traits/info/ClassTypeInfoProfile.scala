package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ClassType
import org.scaladebugger.api.lowlevel.JDIArgument

import scala.util.Try

/**
 * Represents the interface for retrieving class type-based information.
 */
trait ClassTypeInfoProfile extends ReferenceTypeInfoProfile with TypeInfoProfile {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: ClassTypeInfoProfile

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ClassType

  /**
   * Returns a collection of type information for interfaces directly and
   * indirectly implemented by this class.
   *
   * @return The collection of interface type info profiles
   */
  def allInterfaces: Seq[InterfaceTypeInfoProfile]

  /**
   * Returns a collection of type information for interfaces directly and
   * indirectly implemented by this class.
   *
   * @return Success containing the collection of interface type info profiles,
   *         otherwise a failure
   */
  def tryAllInterfaces: Try[Seq[InterfaceTypeInfoProfile]] = Try(allInterfaces)

  /**
   * Returns a collection of interfaces directly implemented by this class.
   *
   * @return The collection of interface type info profiles
   */
  def interfaces: Seq[InterfaceTypeInfoProfile]

  /**
   * Returns a collection of interfaces directly implemented by this class.
   *
   * @return Success containing the collection of interface type info profiles,
   *         otherwise a failure
   */
  def tryInterfaces: Try[Seq[InterfaceTypeInfoProfile]] = Try(interfaces)

  /**
   * Returns the loaded, direct subclasses of this class.
   *
   * @return The collection of class type info profiles
   */
  def subclasses: Seq[ClassTypeInfoProfile]

  /**
   * Returns the superclass of this class.
   *
   * @return Some class type info if the super class exists, otherwise None
   */
  def superclassOption: Option[ClassTypeInfoProfile]

  /**
   * Indicates whether or not this class is a Java enumeration.
   *
   * @return True if it is an enumeration, otherwise false
   */
  def isEnumeration: Boolean

  /**
   * Retrieves the single, non-abstract, visible method on this class with the
   * matching name and JNI signature.
   *
   * @param name The name of the method
   * @param signature The JNI signature of the method
   * @return Some method if found, otherwise None
   */
  def methodOption(name: String, signature: String): Option[MethodInfoProfile]

  /**
   * Retrieves the single, non-abstract, visible method on this class with the
   * matching name and JNI signature.
   *
   * @param name The name of the method
   * @param signature The JNI signature of the method
   * @return Success containing the method if found, otherwise a failure
   */
  def tryMethod(name: String, signature: String): Try[MethodInfoProfile] =
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
    thread: ThreadInfoProfile,
    method: MethodInfoProfile,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ValueInfoProfile] = Try(invokeStaticMethod(
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
  def invokeStaticMethod(
    thread: ThreadInfoProfile,
    method: MethodInfoProfile,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfoProfile

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
    thread: ThreadInfoProfile,
    methodName: String,
    methodSignature: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ValueInfoProfile] = Try(invokeStaticMethod(
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
  def invokeStaticMethod(
    thread: ThreadInfoProfile,
    methodName: String,
    methodSignature: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfoProfile

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
    thread: ThreadInfoProfile,
    constructorName: String,
    constructorSignature: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ObjectInfoProfile] = Try(newInstance(
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
  def newInstance(
    thread: ThreadInfoProfile,
    constructorName: String,
    constructorSignature: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ObjectInfoProfile

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
  def newInstance(
    thread: ThreadInfoProfile,
    constructor: MethodInfoProfile,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ObjectInfoProfile

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
    thread: ThreadInfoProfile,
    constructor: MethodInfoProfile,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): Try[ObjectInfoProfile] = Try(newInstance(
    thread,
    constructor,
    arguments,
    jdiArguments: _*
  ))
}
