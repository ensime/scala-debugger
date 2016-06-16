package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.{InvokeSingleThreadedArgument, JDIArgument}
import org.scaladebugger.api.profiles.traits.info.{InterfaceTypeInfoProfile, ValueInfoProfile, _}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a class type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            class type
 * @param _classType The underlying JDI class type to wrap
 */
class PureClassTypeInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _classType: ClassType
) extends PureReferenceTypeInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  _referenceType = _classType
) with ClassTypeInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ClassType = _classType

  /**
   * Returns a collection of type information for interfaces directly and
   * indirectly implemented by this class.
   *
   * @return The collection of interface type info profiles
   */
  override def allInterfaces: Seq[InterfaceTypeInfoProfile] = {
    import scala.collection.JavaConverters._
    _classType.allInterfaces().asScala.map(newInterfaceTypeProfile)
  }

  /**
   * Returns a collection of interfaces directly implemented by this class.
   *
   * @return The collection of interface type info profiles
   */
  override def interfaces: Seq[InterfaceTypeInfoProfile] = {
    import scala.collection.JavaConverters._
    _classType.interfaces().asScala.map(newInterfaceTypeProfile)
  }

  /**
   * Returns the superclass of this class.
   *
   * @return Some class type info if the super class exists, otherwise None
   */
  override def superclassOption: Option[ClassTypeInfoProfile] =
    Option(_classType.superclass()).map(newClassTypeProfile)

  /**
   * Returns the loaded, direct subclasses of this class.
   *
   * @return The collection of class type info profiles
   */
  override def subclasses: Seq[ClassTypeInfoProfile] = {
    import scala.collection.JavaConverters._
    _classType.subclasses().asScala.map(newClassTypeProfile)
  }

  /**
   * Indicates whether or not this class is a Java enumeration.
   *
   * @return True if it is an enumeration, otherwise false
   */
  override def isEnumeration: Boolean = _classType.isEnum

  /**
   * Retrieves the single, non-abstract, visible method on this class with the
   * matching name and JNI signature.
   *
   * @param name      The name of the method
   * @param signature The JNI signature of the method
   * @return Some method if found, otherwise None
   */
  override def methodOption(
    name: String,
    signature: String
  ): Option[MethodInfoProfile] = Option(_classType.concreteMethodByName(
    name,
    signature
  )).map(newMethodProfile)

  /**
   * Creates a new instance of the class.
   *
   * @param thread       The thread within which to create the new instance
   * @param constructor  The constructor method of the class to invoke
   * @param arguments    The arguments to provide to the constructor
   * @param jdiArguments Optional arguments to provide custom settings to
   *                     the constructor invocation
   * @return The instantiated object
   */
  override def newInstance(
    thread: ThreadInfoProfile,
    constructor: MethodInfoProfile,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ObjectInfoProfile = {
    val t = thread.toJdiInstance
    val c = constructor.toJdiInstance

    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    val virtualMachine = newVirtualMachine()
    val v = arguments.map(virtualMachine.mirrorOf(_: Any))

    val o = jdiArguments.map {
      case InvokeSingleThreadedArgument => ClassType.INVOKE_SINGLE_THREADED
    }.fold(0)(_ | _)

    import scala.collection.JavaConverters._
    val r = _classType.newInstance(t, c, v.asJava, o)
    newObjectProfile(
      objectReference = r,
      threadReference = t,
      virtualMachine = virtualMachine
    )
  }

  /**
   * Creates a new instance of the class.
   *
   * @param thread               The thread within which to create the new instance
   * @param constructorName      The name of the constructor to invoke
   * @param constructorSignature The signature of the constructor to invoke
   * @param arguments            The arguments to provide to the constructor
   * @param jdiArguments         Optional arguments to provide custom settings to
   *                             the constructor invocation
   * @return The instantiated object
   */
  override def newInstance(
    thread: ThreadInfoProfile,
    constructorName: String,
    constructorSignature: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ObjectInfoProfile = newInstance(
    thread,
    methodOption(constructorName, constructorSignature).get,
    arguments,
    jdiArguments: _*
  )

  /**
   * Invokes the class' static method with matching name and arguments.
   *
   * @param thread             The thread within which to invoke the method
   * @param methodName         The name of the method to invoke
   * @param methodSignature    The signature of the method to invoke
   * @param arguments          The arguments to provide to the method
   * @param jdiArguments       Optional arguments to provide custom settings to the
   *                           method invocation
   * @return The resulting value of the invocation
   */
  override def invokeStaticMethod(
    thread: ThreadInfoProfile,
    methodName: String,
    methodSignature: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfoProfile = invokeStaticMethod(
    thread,
    methodOption(methodName, methodSignature).get,
    arguments,
    jdiArguments: _*
  )

  /**
   * Invokes the class' static method.
   *
   * @param thread       The thread within which to invoke the method
   * @param method       The method of the class to invoke
   * @param arguments    The arguments to provide to the method
   * @param jdiArguments Optional arguments to provide custom settings to
   *                     the method invocation
   * @return The resulting value of the invocation
   */
  override def invokeStaticMethod(
    thread: ThreadInfoProfile,
    method: MethodInfoProfile,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfoProfile = {
    val t = thread.toJdiInstance
    val m = method.toJdiInstance

    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    val v = arguments.map(newVirtualMachine().mirrorOf(_: Any))

    val o = jdiArguments.map {
      case InvokeSingleThreadedArgument => ClassType.INVOKE_SINGLE_THREADED
    }.fold(0)(_ | _)

    import scala.collection.JavaConverters._
    val r = _classType.invokeMethod(t, m, v.asJava, o)
    newValueProfile(r)
  }

  protected def newObjectProfile(
    objectReference: ObjectReference,
    threadReference: ThreadReference,
    virtualMachine: VirtualMachine
  ): ObjectInfoProfile = new PureObjectInfoProfile(
    scalaVirtualMachine,
    objectReference
  )(
    _virtualMachine = virtualMachine,
    _threadReference = threadReference,
    _referenceType = objectReference.referenceType()
  )

  protected def newVirtualMachine(): VirtualMachine =
    scalaVirtualMachine.underlyingVirtualMachine

  protected def newValueProfile(value: Value): ValueInfoProfile =
    new PureValueInfoProfile(scalaVirtualMachine, value)
}
