package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{FrameInfoProfile, ThreadInfoProfile, ThreadStatusInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure implementation of a thread profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            thread
 * @param _threadReference The reference to the underlying JDI thread
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 * @param _referenceType The reference type for this thread
 */
class PureThreadInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _threadReference: ThreadReference
)(
  override protected val _virtualMachine: VirtualMachine = _threadReference.virtualMachine(),
  private val _referenceType: ReferenceType = _threadReference.referenceType()
) extends PureObjectInfoProfile(scalaVirtualMachine, _threadReference)(
  _virtualMachine = _virtualMachine,
  _threadReference = _threadReference,
  _referenceType = _referenceType
) with ThreadInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ThreadReference = _threadReference

  /**
   * Represents the name of the thread.
   *
   * @return The thread name as a string
   */
  override def name: String = _threadReference.name()

  /**
   * Represents the information about the thread's status.
   *
   * @return The thread's status as a profile
   */
  override def status: ThreadStatusInfoProfile = newThreadStatusProfile()

  /**
   * Suspends the thread by incrementing the pending suspension counter.
   */
  override def suspend(): Unit = _threadReference.suspend()

  /**
   * Resumes the thread if suspended by decrementing the pending suspension
   * counter. If the counter remains greater than zero, the thread remains
   * suspended.
   */
  override def resume(): Unit = _threadReference.resume()

  /**
   * Retrieves profiles for all frames in the stack.
   *
   * @return The collection of frame profiles
   */
  override def frames: Seq[FrameInfoProfile] = {
    import scala.collection.JavaConverters._
    _threadReference.frames().asScala.zipWithIndex.map { case (f, i) =>
      newFrameProfile(f, i)
    }
  }

  /**
   * Retrieves profiles for all frames in the stack starting from the specified
   * index and up to the desired length.
   *
   * @param index  The index (starting with 0 being top) of the first frame
   *               whose profile to retrieve
   * @param length The total number of frames to retrieve starting with the one
   *               at index
   * @return The collection of frame profiles
   */
  override protected def rawFrames(
    index: Int,
    length: Int
  ): Seq[FrameInfoProfile] = {
    import scala.collection.JavaConverters._

    _threadReference.frames(index, length).asScala
      .zipWithIndex.map { case (f, i) => newFrameProfile(f, i + index) }
  }

  /**
   * Retrieves the profile for the specified frame in the stack.
   *
   * @param index The index (starting with 0 being top) of the frame whose
   *              profile to retrieve
   * @return The new frame profile instance
   */
  override def frame(index: Int): FrameInfoProfile = {
    newFrameProfile(_threadReference.frame(index), index)
  }

  /**
   * Returns the total frames held in the current frame stack.
   *
   * @return The total number of frames
   */
  override def totalFrames: Int = _threadReference.frameCount()

  protected def newFrameProfile(
    stackFrame: StackFrame,
    index: Int
  ): FrameInfoProfile = new PureFrameInfoProfile(
    scalaVirtualMachine,
    stackFrame,
    index
  )

  protected def newThreadStatusProfile(): ThreadStatusInfoProfile =
    new PureThreadStatusInfoProfile(_threadReference)
}
