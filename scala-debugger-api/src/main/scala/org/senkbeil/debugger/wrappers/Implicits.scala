package org.senkbeil.debugger.wrappers

import scala.language.implicitConversions

import com.sun.jdi._

/**
 * Contains helper implicits to convert to wrapper classes.
 */
object Implicits {
  implicit def valueToWrapper(value: Value): ValueWrapper =
    new ValueWrapper(value)

  implicit def stackFrameToWrapper(stackFrame: StackFrame): StackFrameWrapper =
    new StackFrameWrapper(stackFrame)

  implicit def threadReferenceToWrapper(
    threadReference: ThreadReference
  ): ThreadReferenceWrapper = new ThreadReferenceWrapper(threadReference)

  implicit def referenceTypeToWrapper(
    referenceType: ReferenceType
  ): ReferenceTypeWrapper = new ReferenceTypeWrapper(referenceType)

  implicit def virtualMachineToWrapper(
    virtualMachine: VirtualMachine
  ): VirtualMachineWrapper = new VirtualMachineWrapper(virtualMachine)
}
