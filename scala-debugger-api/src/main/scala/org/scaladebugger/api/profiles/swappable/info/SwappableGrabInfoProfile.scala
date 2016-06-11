package org.scaladebugger.api.profiles.swappable.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.info._

/**
 * Represents a swappable profile for grabbing various info that redirects the
 * invocation to another profile.
 */
trait SwappableGrabInfoProfile extends GrabInfoProfile {
  this: SwappableDebugProfileManagement =>
  override def `object`(
    threadReference: ThreadReference,
    objectReference: ObjectReference
  ): ObjectInfoProfile = withCurrentProfile.`object`(
    threadReference,
    objectReference
  )

  override def threads: Seq[ThreadInfoProfile] = withCurrentProfile.threads

  override def thread(
    threadReference: ThreadReference
  ): ThreadInfoProfile = withCurrentProfile.thread(threadReference)

  override def threadOption(
    threadId: Long
  ): Option[ThreadInfoProfile] = withCurrentProfile.threadOption(threadId)

  override def threadGroups: Seq[ThreadGroupInfoProfile] =
    withCurrentProfile.threadGroups

  override def threadGroup(
    threadGroupReference: ThreadGroupReference
  ): ThreadGroupInfoProfile = withCurrentProfile.threadGroup(threadGroupReference)

  override def threadGroupOption(
    threadGroupId: Long
  ): Option[ThreadGroupInfoProfile] = withCurrentProfile.threadGroupOption(threadGroupId)

  override def classes: Seq[ReferenceTypeInfoProfile] =
    withCurrentProfile.classes

  override def classOption(name: String): Option[ReferenceTypeInfoProfile] =
    withCurrentProfile.classOption(name)

  override def `class`(
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = withCurrentProfile.`class`(referenceType)

  override def field(
    referenceType: ReferenceType,
    field: Field
  ): FieldVariableInfoProfile = withCurrentProfile.field(referenceType, field)

  override def field(
    objectReference: ObjectReference,
    field: Field
  ): FieldVariableInfoProfile = withCurrentProfile.field(objectReference, field)

  override def localVariable(
    stackFrame: StackFrame,
    localVariable: LocalVariable
  ): VariableInfoProfile = withCurrentProfile.localVariable(
    stackFrame,
    localVariable
  )

  override def location(location: Location): LocationInfoProfile =
    withCurrentProfile.location(location)

  override def method(method: Method): MethodInfoProfile =
    withCurrentProfile.method(method)

  override def stackFrame(stackFrame: StackFrame): FrameInfoProfile =
    withCurrentProfile.stackFrame(stackFrame)

  override def `type`(_type: Type): TypeInfoProfile =
    withCurrentProfile.`type`(_type)

  override def value(value: Value): ValueInfoProfile =
    withCurrentProfile.value(value)
}
