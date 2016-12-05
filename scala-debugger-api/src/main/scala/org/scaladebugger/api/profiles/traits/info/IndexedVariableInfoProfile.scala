package org.scaladebugger.api.profiles.traits.info


/**
 * Represents the interface for variable-based interaction with indexed
 * location information.
 */
trait IndexedVariableInfoProfile
  extends VariableInfoProfile with CreateInfoProfile with CommonInfoProfile
{
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: IndexedVariableInfoProfile

  /**
   * Returns the frame containing this variable.
   *
   * @return The profile of the frame
   */
  def frame: FrameInfoProfile

  /**
   * Returns the index of the stack frame where this variable is located.
   *
   * @return The frame starting from 0 (top of the stack)
   */
  def frameIndex: Int
}
