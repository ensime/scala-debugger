package org.scaladebugger.api.dsl.info

import org.scaladebugger.api.profiles.traits.info.{ObjectInfo, ThreadInfo, VariableInfo, FrameInfo}

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param frameInfo The frame info to wrap
 */
class FrameInfoDSLWrapper private[dsl] (
  private val frameInfo: FrameInfo
) {
  /** @see FrameInfo#tryThisObject() */
  def withThisObject: Try[ObjectInfo] = frameInfo.tryThisObject

  /** @see FrameInfo#thisObject() */
  def withUnsafeThisObject: ObjectInfo = frameInfo.thisObject

  /** @see FrameInfo#tryCurrentThread() */
  def withCurrentThread: Try[ThreadInfo] =
    frameInfo.tryCurrentThread

  /** @see FrameInfo#currentThread() */
  def withUnsafeCurrentThread: ThreadInfo =
    frameInfo.currentThread

  /** @see FrameInfo#tryVariable(String) */
  def forVariable(name: String): Try[VariableInfo] =
    frameInfo.tryVariable(name)

  /** @see FrameInfo#variable(String) */
  def forUnsafeVariable(name: String): VariableInfo =
    frameInfo.variable(name)

  /** @see FrameInfo#tryAllVariables() */
  def forAllVariables: Try[Seq[VariableInfo]] =
    frameInfo.tryAllVariables

  /** @see FrameInfo#tryArgumentLocalVariables() */
  def forArguments: Try[Seq[VariableInfo]] =
    frameInfo.tryArgumentLocalVariables

  /** @see FrameInfo#tryNonArgumentLocalVariables() */
  def forNonArguments: Try[Seq[VariableInfo]] =
    frameInfo.tryNonArgumentLocalVariables

  /** @see FrameInfo#tryLocalVariables() */
  def forLocalVariables: Try[Seq[VariableInfo]] =
    frameInfo.tryLocalVariables

  /** @see FrameInfo#tryFieldVariables() */
  def forFieldVariables: Try[Seq[VariableInfo]] =
    frameInfo.tryFieldVariables

  /** @see FrameInfo#allVariables() */
  def forUnsafeAllVariables: Seq[VariableInfo] =
    frameInfo.allVariables

  /** @see FrameInfo#argumentLocalVariables() */
  def forUnsafeArguments: Seq[VariableInfo] =
    frameInfo.argumentLocalVariables

  /** @see FrameInfo#nonArgumentLocalVariables() */
  def forUnsafeNonArguments: Seq[VariableInfo] =
    frameInfo.nonArgumentLocalVariables

  /** @see FrameInfo#localVariables() */
  def forUnsafeLocalVariables: Seq[VariableInfo] =
    frameInfo.localVariables

  /** @see FrameInfo#fieldVariables() */
  def forUnsafeFieldVariables: Seq[VariableInfo] =
    frameInfo.fieldVariables
}
