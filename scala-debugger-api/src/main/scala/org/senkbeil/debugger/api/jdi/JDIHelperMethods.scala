package org.senkbeil.debugger.api.jdi

import org.senkbeil.debugger.api.utils.LogLike
import com.sun.jdi.{ReferenceType, ThreadReference, VirtualMachine}

import scala.collection.JavaConverters._
import scala.util.Try

trait JDIHelperMethods extends LogLike {
  protected val _virtualMachine: VirtualMachine

  /**
   * Executes the provided block of code during the state of a suspended
   * virtual machine. The virtual machine is synchronized to prevent another
   * thread suspending the same virtual machine.
   *
   * @param thunk The block of code to execute
   * @tparam T The type of result from the block of code
   *
   * @return The results of attempting to execute the block of code
   */
  protected def suspendVirtualMachineAndExecute[T](thunk: => T) =
    _virtualMachine.synchronized {
      // Pause the virtual machine while we perform the operation
      _virtualMachine.suspend()

      // Execute the operation
      val result = Try(thunk)

      // Resume the virtual machine now that the operation has been completed
      _virtualMachine.resume()

      // Return the result of the operation
      result
    }

  /**
   * Executes the provided block of code during the state of a suspended
   * thread. The thread reference is synchronized to prevent another thread
   * suspending the same thread reference.
   *
   * @param thunk The block of code to execute
   * @tparam T The type of result from the block of code
   *
   * @return The results of attempting to execute the block of code
   */
  protected def suspendThreadAndExecute[T](
    threadReference: ThreadReference
  )(thunk: => T): Try[T] = threadReference.synchronized {
    // Pause the thread while we perform the operation
    threadReference.suspend()

    // Execute the operation
    val result = Try(thunk)

    // Resume the thread now that the operation has been completed
    threadReference.resume()

    // Return the result of the operation
    result
  }

  /**
   * Determines the main executing thread of the _virtualMachine instance.
   *
   * @return Some reference to the main thread if it can be determined,
   *         otherwise None
   */
  protected def findMainThread(): Option[ThreadReference] =
    findMainThread(_virtualMachine)

  /**
   * Determines the main executing thread of the specified virtual machine.
   *
   * @param virtualMachine The virtual machine whose main thread to determine
   *
   * @return Some reference to the main thread if it can be determined,
   *         otherwise None
   */
  protected def findMainThread(
    virtualMachine: VirtualMachine
  ): Option[ThreadReference] =
    virtualMachine.allThreads().asScala.find(_.name() == "main")

  /**
   * Retrieves the convergent source path of the provided reference type.
   *
   * @param referenceType The reference instance whose source path to determine
   *
   * @return Some source path as a string if convergent, otherwise None
   */
  protected def singleSourcePath(
    referenceType: ReferenceType
  ): Option[String] = {
    val trySourcePaths =
      Try(referenceType.sourcePaths(_virtualMachine.getDefaultStratum).asScala)

    val sourcePath = trySourcePaths.map(sourcePaths => {
      sourcePaths.foldLeft(sourcePaths.head) {
        case (a, b) =>
          // If we have different paths, there is no way to determine a full
          // original class name
          assert(a == b, "Source paths are divergent!")

          // Should all be the same
          b
      }
    })

    sourcePath.toOption
  }
}
