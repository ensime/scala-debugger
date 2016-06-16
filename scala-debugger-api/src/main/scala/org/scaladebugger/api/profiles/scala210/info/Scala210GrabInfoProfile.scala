package org.scaladebugger.api.profiles.scala210.info

//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.pure.info.PureGrabInfoProfile
import org.scaladebugger.api.profiles.traits.info._

/**
 * Represents a Scala 2.10 profile for grabbing various information from threads
 * and other objects.
 */
trait Scala210GrabInfoProfile extends PureGrabInfoProfile {
  override protected def newThreadProfile(
    threadReference: ThreadReference
  ): ThreadInfoProfile = new Scala210ThreadInfoProfile(
    scalaVirtualMachine,
    threadReference
  )(_virtualMachine = _virtualMachine)

  override protected def newThreadGroupProfile(
    threadGroupReference: ThreadGroupReference
  ): ThreadGroupInfoProfile = new Scala210ThreadGroupInfoProfile(
    scalaVirtualMachine,
    threadGroupReference
  )(_virtualMachine = _virtualMachine)

  override protected def newReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = new Scala210ReferenceTypeInfoProfile(
    scalaVirtualMachine,
    referenceType
  )

  override protected def newTypeProfile(_type: Type): TypeInfoProfile =
    new Scala210TypeInfoProfile(scalaVirtualMachine, _type)

  override protected def newValueProfile(value: Value): ValueInfoProfile =
    new Scala210ValueInfoProfile(scalaVirtualMachine, value)

  override protected def newLocationProfile(location: Location): LocationInfoProfile =
    new Scala210LocationInfoProfile(scalaVirtualMachine, location)

  override protected def newMethodProfile(method: Method): MethodInfoProfile =
    new Scala210MethodInfoProfile(scalaVirtualMachine, method)

  override protected def newFrameProfile(stackFrame: StackFrame): FrameInfoProfile =
    new Scala210FrameInfoProfile(scalaVirtualMachine, stackFrame, -1)

  override protected def newFieldProfile(
    objectReference: ObjectReference,
    field: Field
  ): FieldVariableInfoProfile = new Scala210FieldInfoProfile(
    scalaVirtualMachine,
    Left(objectReference),
    field,
    -1
  )(_virtualMachine)

  override protected def newFieldProfile(
    referenceType: ReferenceType,
    field: Field
  ): FieldVariableInfoProfile = new Scala210FieldInfoProfile(
    scalaVirtualMachine,
    Right(referenceType),
    field,
    -1
  )(_virtualMachine)

  override protected def newLocalVariableProfile(
    stackFrame: StackFrame,
    localVariable: LocalVariable
  ): VariableInfoProfile = new Scala210LocalVariableInfoProfile(
    scalaVirtualMachine,
    newFrameProfile(stackFrame),
    localVariable,
    -1
  )(_virtualMachine)

  override protected def newObjectProfile(
    threadReference: ThreadReference,
    objectReference: ObjectReference
  ): ObjectInfoProfile = new Scala210ObjectInfoProfile(
    scalaVirtualMachine,
    objectReference
  )(
    _threadReference = threadReference,
    _virtualMachine = _virtualMachine
  )
}
