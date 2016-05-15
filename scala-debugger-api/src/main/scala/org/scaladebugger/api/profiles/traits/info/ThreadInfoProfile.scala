package org.scaladebugger.api.profiles.traits.info
//import acyclic.file

import com.sun.jdi.ThreadReference

import scala.util.Try

/**
 * Represents the interface for thread-based interaction.
 */
trait ThreadInfoProfile extends ObjectInfoProfile with CommonInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ThreadReference

  /**
   * Represents the name of the thread.
   *
   * @return The thread name as a string
   */
  def name: String

  /**
   * Represents the information about the thread's status.
   *
   * @return The thread's status as a profile
   */
  def status: ThreadStatusInfoProfile

  /**
   * Represents the thread group containing this thread.
   *
   * @return The profile of the thread group
   */
  def threadGroup: ThreadGroupInfoProfile

  /**
   * Resumes the thread if suspended by decrementing the pending suspension
   * counter. If the counter remains greater than zero, the thread remains
   * suspended.
   */
  def resume(): Unit

  /**
   * Suspends the thread by incrementing the pending suspension counter.
   */
  def suspend(): Unit

  /**
   * Retrieves profiles for all frames in the stack.
   *
   * @return Success of collection of frame profiles, otherwise a failure
   */
  def tryFrames: Try[Seq[FrameInfoProfile]] = Try(frames)

  /**
   * Retrieves profiles for all frames in the stack.
   *
   * @return The collection of frame profiles
   */
  def frames: Seq[FrameInfoProfile]

  /**
   * Retrieves profiles for all frames in the stack starting from the specified
   * index and up to the desired length.
   *
   * @param index The index (starting with 0 being top) of the first frame
   *              whose profile to retrieve
   * @param length The total number of frames to retrieve starting with the one
   *               at index, or -1 if all frames including and after the index
   *               should be retrieved
   * @return Success of collection of frame profiles, otherwise a failure
   */
  def tryFrames(index: Int, length: Int): Try[Seq[FrameInfoProfile]] =
    Try(frames(index, length))

  /**
   * Retrieves profiles for all frames in the stack starting from the specified
   * index and up to the desired length.
   *
   * @param index The index (starting with 0 being top) of the first frame
   *              whose profile to retrieve
   * @param length The total number of frames to retrieve starting with the one
   *               at index, or -1 if all frames including and after the index
   *               should be retrieved
   * @return The collection of frame profiles
   */
  def frames(index: Int, length: Int): Seq[FrameInfoProfile] = {
    // Cap the length to prevent throwing an exception on index out of bounds
    // with regard to desired total frames
    val maxFrames = totalFrames
    val actualLength =
      if (index + length > maxFrames) maxFrames - index
      else if (length < 0) maxFrames - index
      else length

    rawFrames(index, actualLength)
  }

  /**
   * Retrieves profiles for all frames in the stack starting from the specified
   * index and up to the desired length.
   *
   * @param index The index (starting with 0 being top) of the first frame
   *              whose profile to retrieve
   * @param length The total number of frames to retrieve starting with the one
   *               at index
   * @return The collection of frame profiles
   */
  protected def rawFrames(index: Int, length: Int): Seq[FrameInfoProfile]

  /**
   * Returns the total frames held in the current frame stack.
   *
   * @return Success containing the total number of frames, otherwise a failure
   */
  def tryTotalFrames: Try[Int] = Try(totalFrames)

  /**
   * Returns the total frames held in the current frame stack.
   *
   * @return The total number of frames
   */
  def totalFrames: Int

  /**
   * Retrieves the profile for the specified frame in the stack.
   *
   * @param index The index (starting with 0 being top) of the frame whose
   *              profile to retrieve
   * @return Success containing the new frame profile instance, otherwise
   *         a failure
   */
  def tryFrame(index: Int): Try[FrameInfoProfile] = Try(frame(index))

  /**
   * Retrieves the profile for the specified frame in the stack.
   *
   * @param index The index (starting with 0 being top) of the frame whose
   *              profile to retrieve
   * @return The new frame profile instance
   */
  def frame(index: Int): FrameInfoProfile

  /**
   * Retrieves the profile for the top (current) frame in the stack.
   *
   * @return Success containing the new frame profile instance, otherwise
   *         a failure
   */
  def tryTopFrame: Try[FrameInfoProfile] = tryFrame(0)

  /**
   * Retrieves the profile for the top (current) frame in the stack.
   *
   * @return The new frame profile instance
   */
  def topFrame: FrameInfoProfile = frame(0)

  /**
   * Retrieves an active variable within the thread's stack frames with the
   * matching name.
   *
   * @param name The name of the variable to find
   * @return Some variable if found, otherwise None
   */
  def findVariableByName(name: String): Option[VariableInfoProfile] = {
    // NOTE: Using for loop to reduce data retrieval (finding earlier is better)
    for (frameIndex <- 0 until this.totalFrames) {
      val frame = this.frame(frameIndex)
      val variable = frame.tryIndexedVariable(name).toOption
      if (variable.nonEmpty) return variable
    }

    // No variable found
    None
  }

  /**
   * Retrieves an active variable within the thread's stack frames with the
   * matching name.
   *
   * @param name The name of the variable to find
   * @return Success containing the variable if found, otherwise a failure
   */
  def tryFindVariableByName(name: String): Try[VariableInfoProfile] =
    Try(findVariableByName(name).get)

  /**
   * Retrieves an active variable from the specified stack frame using its
   * index and the offset of visible, local variables in the stack frame.
   *
   * @param frameIndex The index of the frame containing the variable
   * @param offsetIndex The offset within the frame to find the variable
   * @return Some variable if found, otherwise None
   */
  def findVariableByIndex(
    frameIndex: Int,
    offsetIndex: Int
  ): Option[VariableInfoProfile] = {
    this.frame(frameIndex)
      .indexedLocalVariables
      .find(_.offsetIndex == offsetIndex)
  }

  /**
   * Retrieves an active variable from the specified stack frame using its
   * index and the offset of visible, local variables in the stack frame.
   *
   * @param frameIndex The index of the frame containing the variable
   * @param offsetIndex The offset within the frame to find the variable
   * @return Some variable if found, otherwise None
   */
  def tryFindVariableByIndex(
    frameIndex: Int,
    offsetIndex: Int
  ): Try[VariableInfoProfile] =
    Try(findVariableByIndex(frameIndex, offsetIndex).get)

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    s"Thread $name (0x$uniqueIdHexString)"
  }
}
