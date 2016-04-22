package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.{InvokeNonVirtualArgument, InvokeSingleThreadedArgument, JDIArgument}
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try
import scala.collection.JavaConverters._

/**
 * Represents a pure implementation of an object profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            object
 * @param _objectReference The reference to the underlying JDI object
 * @param _virtualMachine The virtual machine associated with the object
 * @param _threadReference The thread associated with the object (for method
 *                        invocation)
 * @param _referenceType The reference type for this object
 */
class PureObjectInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _objectReference: ObjectReference
)(
  protected val _virtualMachine: VirtualMachine = _objectReference.virtualMachine(),
  private val _threadReference: ThreadReference = _objectReference.owningThread(),
  private val _referenceType: ReferenceType = _objectReference.referenceType()
) extends PureValueInfoProfile(
  scalaVirtualMachine,
  _objectReference
) with ObjectInfoProfile {
  private lazy val typeChecker = newTypeCheckerProfile()

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ObjectReference = _objectReference

  /**
   * Returns the type information for the object.
   *
   * @return The profile containing type information
   */
  override def typeInfo: ReferenceTypeInfoProfile =
    super.typeInfo.toReferenceType

  /**
   * Represents the unique id of this object.
   *
   * @return The unique id as a long
   */
  override def uniqueId: Long = _objectReference.uniqueID()

  /**
   * Returns the reference type information for this object.
   *
   * @note Returns the specific type of this object, not any interface or
   *       superclass that it inherits. So, val x: AnyRef = "a string" would
   *       yield the reference type for String, not AnyRef.
   * @return The reference type information
   */
  override def referenceType: ReferenceTypeInfoProfile =
    newTypeProfile(_objectReference.referenceType()).toReferenceType

  /**
   * Invokes the object's method.
   *
   * @param method The method of the object to invoke
   * @param arguments         The arguments to provide to the method
   * @param jdiArguments      Optional arguments to provide custom settings to the
   *                          method invocation
   * @return The resulting value of the invocation
   */
  override def invoke(
    method: MethodInfoProfile,
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfoProfile = {
    val m = method.toJdiInstance

    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    val v = arguments.map(_virtualMachine.mirrorOf(_: Any))

    val o = jdiArguments.map {
      case InvokeSingleThreadedArgument => ObjectReference.INVOKE_SINGLE_THREADED
      case InvokeNonVirtualArgument     => ObjectReference.INVOKE_NONVIRTUAL
    }.fold(0)(_ | _)

    val r = _objectReference.invokeMethod(_threadReference, m, v.asJava, o)
    newValueProfile(r)
  }

  /**
   * Invokes the object's method with matching name and arguments.
   *
   * @param methodName         The name of the method to invoke
   * @param parameterTypeNames The names of the parameter types of the method
   *                           to invoke
   * @param arguments          The arguments to provide to the method
   * @param jdiArguments       Optional arguments to provide custom settings to
   *                           the method invocation
   * @return The resulting value of the invocation
   */
  override def invoke(
    methodName: String,
    parameterTypeNames: Seq[String],
    arguments: Seq[Any],
    jdiArguments: JDIArgument*
  ): ValueInfoProfile = {
    assert(parameterTypeNames.length == arguments.length,
      "Inconsistent number of parameter types versus arguments!")

    invoke(
      method(methodName, parameterTypeNames: _*),
      arguments,
      jdiArguments: _*
    )
  }

  /**
   * Returns all visible methods contained in this object.
   *
   * @return The profiles wrapping the visible methods in this object
   */
  override def methods: Seq[MethodInfoProfile] = {
    _referenceType.visibleMethods().asScala.map(newMethodProfile)
  }

  /**
   * Returns the object's method with the specified name.
   *
   * @param name               The name of the method
   * @param parameterTypeNames The fully-qualified type names of the parameters
   *                           of the method to find
   * @return The profile wrapping the method
   */
  override def method(
    name: String,
    parameterTypeNames: String*
  ): MethodInfoProfile = {
    _referenceType.methodsByName(name).asScala.find(
      _.argumentTypeNames().asScala.zip(parameterTypeNames)
        .forall(s => typeChecker.equalTypeNames(s._1, s._2))
    ).map(newMethodProfile).get
  }

  /**
   * Returns all visible fields contained in this object.
   *
   * @return The profiles wrapping the visible fields in this object
   */
  override def fields: Seq[VariableInfoProfile] = {
    _referenceType.visibleFields().asScala.map(newFieldProfile)
  }

  /**
   * Returns the object's field with the specified name.
   *
   * @param name The name of the field
   * @return The profile wrapping the field
   */
  override def field(name: String): VariableInfoProfile = {
    newFieldProfile(Option(_referenceType.fieldByName(name)).get)
  }

  protected def newFieldProfile(field: Field): VariableInfoProfile =
    new PureFieldInfoProfile(
      scalaVirtualMachine,
      Left(_objectReference),
      field
    )()

  protected def newMethodProfile(method: Method): MethodInfoProfile =
    new PureMethodInfoProfile(scalaVirtualMachine, method)

  protected def newValueProfile(value: Value): ValueInfoProfile =
    new PureValueInfoProfile(scalaVirtualMachine, value)

  protected def newTypeCheckerProfile(): TypeCheckerProfile =
    new PureTypeCheckerProfile
}
