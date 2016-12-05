package org.scaladebugger.api.lowlevel.utils

import com.sun.jdi._
import org.scaladebugger.api.utils.Logging

import scala.collection.JavaConverters._
import scala.util.Try

trait JDIHelperMethods extends Logging {
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

  /**
   * Retrieves the fully-qualified class name that invoked the main method of
   * this virtual machine.
   *
   * @return The name as a string
   */
  protected def retrieveMainClassName(): String = {
    val mainThread = findMainThread().get

    val tryClassName = suspendThreadAndExecute(mainThread) {
      val mainMethodFrames = mainThread.frames().asScala
        .map(_.location()).filter(_.method().name() == "main").toSeq

      assert(mainMethodFrames.nonEmpty, "Error locating main method!")

      // NOTE: This is a simple fix to catch MyObject vs MyObject$, but does
      //       not guarantee anything with scala.DelayedInit via scala.App,
      //       meaning that applications started using that trait might return
      //       the wrong class name (scala.App$class)
      val mainMethodFrame = mainMethodFrames.reduce((loc1, loc2) => {
        val loc1DeclaringType = loc1.declaringType().name()
        val loc2DeclaringType = loc2.declaringType().name()

        // Return location that is furthest up the class chain (ignore
        // Scala's generated classes like MyObject$ class for MyObject object)
        if (loc1DeclaringType.contains(loc2DeclaringType)) loc2 else loc1
      })

      mainMethodFrame
    }.map(_.declaringType().name())

    // Throw our exception if we get one
    tryClassName.failed.foreach(ex => throw ex)

    // Return the resulting class name
    tryClassName.get
  }

  /**
   * Retrieves the command line arguments used to start this virtual machine.
   *
   * @return The sequence of arguments as strings
   */
  protected def retrieveCommandLineArguments(): Seq[String] = {
    def processArguments(values: Seq[Value]): Seq[String] = {
      values.flatMap {
        // Should represent the whole array of string arguments, drill down
        case arrayReference: ArrayReference =>
          processArguments(arrayReference.getValues.asScala)

        // Base structure (string) should be returned as an argument
        case stringReference: StringReference =>
          Seq(stringReference.value())

        // NOTE: A reference to the underlying class tends to show up as an
        // additional value after the virtual machine is initialized, so we
        // want to ignore it without flooding our logging output
        case objectReference: ObjectReference => Nil

        // Ignore any other values (some show up due to Scala)
        case v =>
          logger.warn(s"Unknown value while processing arguments: $v")
          Nil
      }
    }

    // Get the main thread of execution
    val mainThread = findMainThread().get

    // Retrieve command line arguments for connected JVM
    val tryArguments = suspendThreadAndExecute(mainThread) {
      val arguments = mainThread.frames().asScala
        .find(_.location().method().name() == "main")
        .map(_.getArgumentValues.asScala.toSeq)
        .map(processArguments)

      assert(arguments.nonEmpty, "Error locating main method!")

      arguments.get
    }

    // Throw our exception if we get one
    tryArguments.failed.foreach(ex => throw ex)

    // Return the resulting arguments
    tryArguments.get
  }
}
