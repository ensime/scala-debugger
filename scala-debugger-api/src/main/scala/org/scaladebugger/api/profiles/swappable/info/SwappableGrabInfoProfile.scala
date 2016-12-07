package org.scaladebugger.api.profiles.swappable.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.info._

/**
 * Represents a swappable profile for grabbing various info that redirects the
 * invocation to another profile.
 */
trait SwappableGrabInfoProfile extends GrabInfoProfile {
  this: SwappableDebugProfileManagement =>
  override def `object`(objectReference: ObjectReference): ObjectInfo =
    withCurrentProfile.`object`(objectReference)

  override def threads: Seq[ThreadInfo] = withCurrentProfile.threads

  override def thread(
    threadReference: ThreadReference
  ): ThreadInfo = withCurrentProfile.thread(threadReference)

  override def threadOption(
    threadId: Long
  ): Option[ThreadInfo] = withCurrentProfile.threadOption(threadId)

  override def threadGroups: Seq[ThreadGroupInfo] =
    withCurrentProfile.threadGroups

  override def threadGroup(
    threadGroupReference: ThreadGroupReference
  ): ThreadGroupInfo = withCurrentProfile.threadGroup(threadGroupReference)

  override def threadGroupOption(
    threadGroupId: Long
  ): Option[ThreadGroupInfo] = withCurrentProfile.threadGroupOption(threadGroupId)

  override def threadGroupOption(
    name: String
  ): Option[ThreadGroupInfo] = withCurrentProfile.threadGroupOption(name)

  override def classes: Seq[ReferenceTypeInfo] =
    withCurrentProfile.classes

  override def classOption(name: String): Option[ReferenceTypeInfo] =
    withCurrentProfile.classOption(name)

  override def `class`(
    referenceType: ReferenceType
  ): ReferenceTypeInfo = withCurrentProfile.`class`(referenceType)

  override def field(
    referenceType: ReferenceType,
    field: Field
  ): FieldVariableInfo = withCurrentProfile.field(referenceType, field)

  override def field(
    objectReference: ObjectReference,
    field: Field
  ): FieldVariableInfo = withCurrentProfile.field(objectReference, field)

  override def localVariable(
    stackFrame: StackFrame,
    localVariable: LocalVariable
  ): VariableInfo = withCurrentProfile.localVariable(
    stackFrame,
    localVariable
  )

  override def location(location: Location): LocationInfo =
    withCurrentProfile.location(location)

  override def method(method: Method): MethodInfo =
    withCurrentProfile.method(method)

  override def stackFrame(stackFrame: StackFrame): FrameInfo =
    withCurrentProfile.stackFrame(stackFrame)

  override def `type`(_type: Type): TypeInfo =
    withCurrentProfile.`type`(_type)

  override def value(value: Value): ValueInfo =
    withCurrentProfile.value(value)
}
