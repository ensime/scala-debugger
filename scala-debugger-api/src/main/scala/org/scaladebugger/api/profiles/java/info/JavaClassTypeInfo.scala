package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.{InvokeSingleThreadedArgument, JDIArgument}
import org.scaladebugger.api.profiles.traits.info.{InterfaceTypeInfo, ValueInfo, _}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a java implementation of a class type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            class type
 * @param infoProducer The producer of info-based profile instances
 * @param _classType The underlying JDI class type to wrap
 */
class JavaClassTypeInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val _classType: ClassType
) extends JavaReferenceTypeInfo(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  _referenceType = _classType
) with ClassTypeInfo {
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
  override def toJavaInfo: ClassTypeInfo = {
    infoProducer.toJavaInfo.newClassTypeInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      classType = _classType
    )
  }

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
  override def allInterfaces: Seq[InterfaceTypeInfo] = {
    import scala.collection.JavaConverters._
    _classType.allInterfaces().asScala.map(newInterfaceTypeProfile)
  }

  /**
   * Returns a collection of interfaces directly implemented by this class.
   *
   * @return The collection of interface type info profiles
   */
  override def interfaces: Seq[InterfaceTypeInfo] = {
    import scala.collection.JavaConverters._
    _classType.interfaces().asScala.map(newInterfaceTypeProfile)
  }

  /**
   * Returns the superclass of this class.
   *
   * @return Some class type info if the super class exists, otherwise None
   */
  override def superclassOption: Option[ClassTypeInfo] =
    Option(_classType.superclass()).map(newClassTypeProfile)

  /**
   * Returns the loaded, direct subclasses of this class.
   *
   * @return The collection of class type info profiles
   */
  override def subclasses: Seq[ClassTypeInfo] = {
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
  ): Option[MethodInfo] = Option(_classType.concreteMethodByName(
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
    thread: ThreadInfo,
    constructor: MethodInfo,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ObjectInfo = {
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
    thread: ThreadInfo,
    constructorName: String,
    constructorSignature: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ObjectInfo = newInstance(
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
    thread: ThreadInfo,
    methodName: String,
    methodSignature: String,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfo = invokeStaticMethod(
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
    thread: ThreadInfo,
    method: MethodInfo,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfo = {
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
    virtualMachine: VirtualMachine
  ): ObjectInfo = infoProducer.newObjectInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    objectReference = objectReference
  )(
    virtualMachine = virtualMachine
  )

  protected def newVirtualMachine(): VirtualMachine =
    scalaVirtualMachine.underlyingVirtualMachine

  protected def newValueProfile(value: Value): ValueInfo =
    infoProducer.newValueInfo(scalaVirtualMachine, value)
}
