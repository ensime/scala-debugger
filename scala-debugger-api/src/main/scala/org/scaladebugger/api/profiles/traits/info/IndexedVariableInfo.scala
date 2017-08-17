package org.scaladebugger.api.profiles.traits.info

import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
import org.scaladebugger.macros.freeze.{CanFreeze, CannotFreeze, Freezable}


/**
 * Represents the interface for variable-based interaction with indexed
 * location information.
 */
@Freezable
trait IndexedVariableInfo extends VariableInfo with CreateInfo with CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  @CannotFreeze
  override def toJavaInfo: IndexedVariableInfo

  /**
   * Returns the frame containing this variable.
   *
   * @return The profile of the frame
   */
  @CanFreeze(ReturnType.FreezeObject)
  def frame: FrameInfo

  /**
   * Returns the index of the stack frame where this variable is located.
   *
   * @return The frame starting from 0 (top of the stack)
   */
  @CanFreeze
  def frameIndex: Int
}
