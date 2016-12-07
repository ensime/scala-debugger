package org.scaladebugger.api.dsl.info

import org.scaladebugger.api.profiles.traits.info.{ObjectInfo, ThreadInfo, VariableInfo, FrameInfo}

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param frameInfoProfile The profile to wrap
 */
class FrameInfoDSLWrapper private[dsl] (
  private val frameInfoProfile: FrameInfo
) {
  /** @see FrameInfo#tryThisObject() */
  def withThisObject: Try[ObjectInfo] = frameInfoProfile.tryThisObject

  /** @see FrameInfo#thisObject() */
  def withUnsafeThisObject: ObjectInfo = frameInfoProfile.thisObject

  /** @see FrameInfo#tryCurrentThread() */
  def withCurrentThread: Try[ThreadInfo] =
    frameInfoProfile.tryCurrentThread

  /** @see FrameInfo#currentThread() */
  def withUnsafeCurrentThread: ThreadInfo =
    frameInfoProfile.currentThread

  /** @see FrameInfo#tryVariable(String) */
  def forVariable(name: String): Try[VariableInfo] =
    frameInfoProfile.tryVariable(name)

  /** @see FrameInfo#variable(String) */
  def forUnsafeVariable(name: String): VariableInfo =
    frameInfoProfile.variable(name)

  /** @see FrameInfo#tryAllVariables() */
  def forAllVariables: Try[Seq[VariableInfo]] =
    frameInfoProfile.tryAllVariables

  /** @see FrameInfo#tryArgumentLocalVariables() */
  def forArguments: Try[Seq[VariableInfo]] =
    frameInfoProfile.tryArgumentLocalVariables

  /** @see FrameInfo#tryNonArgumentLocalVariables() */
  def forNonArguments: Try[Seq[VariableInfo]] =
    frameInfoProfile.tryNonArgumentLocalVariables

  /** @see FrameInfo#tryLocalVariables() */
  def forLocalVariables: Try[Seq[VariableInfo]] =
    frameInfoProfile.tryLocalVariables

  /** @see FrameInfo#tryFieldVariables() */
  def forFieldVariables: Try[Seq[VariableInfo]] =
    frameInfoProfile.tryFieldVariables

  /** @see FrameInfo#allVariables() */
  def forUnsafeAllVariables: Seq[VariableInfo] =
    frameInfoProfile.allVariables

  /** @see FrameInfo#argumentLocalVariables() */
  def forUnsafeArguments: Seq[VariableInfo] =
    frameInfoProfile.argumentLocalVariables

  /** @see FrameInfo#nonArgumentLocalVariables() */
  def forUnsafeNonArguments: Seq[VariableInfo] =
    frameInfoProfile.nonArgumentLocalVariables

  /** @see FrameInfo#localVariables() */
  def forUnsafeLocalVariables: Seq[VariableInfo] =
    frameInfoProfile.localVariables

  /** @see FrameInfo#fieldVariables() */
  def forUnsafeFieldVariables: Seq[VariableInfo] =
    frameInfoProfile.fieldVariables
}
