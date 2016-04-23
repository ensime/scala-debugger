package org.scaladebugger.api.profiles.traits.info

//import acyclic.file

/**
 * Represents the interface for variable-based interaction with indexed
 * location information.
 */
trait IndexedVariableInfoProfile
  extends VariableInfoProfile with CreateInfoProfile with CommonInfoProfile
{

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
