package org.scaladebugger.api.profiles.swappable.info
//import acyclic.file

import com.sun.jdi.{ObjectReference, ThreadReference}
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

  override def classes: Seq[ReferenceTypeInfoProfile] =
    withCurrentProfile.classes

  override def classOption(name: String): Option[ReferenceTypeInfoProfile] =
    withCurrentProfile.classOption(name)
}
