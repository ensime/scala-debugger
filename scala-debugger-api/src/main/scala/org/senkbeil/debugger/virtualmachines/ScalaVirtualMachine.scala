package org.senkbeil.debugger.virtualmachines

import org.senkbeil.debugger.breakpoints.BreakpointManager
import org.senkbeil.debugger.classes.ClassManager
import org.senkbeil.debugger.events.{EventManager, LoopingTaskRunner}
import org.senkbeil.debugger.jdi.JDIHelperMethods
import org.senkbeil.debugger.steps.StepManager
import org.senkbeil.debugger.wrappers.Implicits._
import org.senkbeil.utils.LogLike
import com.sun.jdi._
import com.sun.jdi.event.ClassPrepareEvent

import scala.collection.JavaConverters._

/**
 * Represents a virtual machine running Scala code.
 *
 * @param _virtualMachine The underlying virtual machine
 * @param loopingTaskRunner The runner used to process events
 * @param uniqueId A unique id assigned to the Scala virtual machine on the
 *                 client (library) side to help distinguish multiple VMs
 */
class ScalaVirtualMachine(
  protected val _virtualMachine: VirtualMachine,
  private val loopingTaskRunner: LoopingTaskRunner,
  val uniqueId: String = java.util.UUID.randomUUID().toString
) extends JDIHelperMethods with LogLike {
  /** Builds a string with the identifier of this virtual machine. */
  private def vmString(message: String) = s"(Scala VM $uniqueId) $message"

  logger.debug(vmString("Initializing Scala virtual machine!"))

  /**
   * Initializes extra events that this virtual machine should receive.
   *
   * @note Override this method to alter the extra events received!
   */
  protected def initializeEvents(): Unit = {
    _virtualMachine.enableClassPrepareEvents()
    _virtualMachine.enableThreadStartEvents()
    _virtualMachine.enableThreadDeathEvents()
    _virtualMachine.enableExceptionEvents()
  }
  initializeEvents()

  // Lazily-load the class manager (and as a result, the other managers) to
  // give enough time to retrieve all of the classes
  lazy val classManager =
    new ClassManager(_virtualMachine, loadClasses = true)
  lazy val stepManager = new StepManager(_virtualMachine)
  lazy val breakpointManager =
    new BreakpointManager(_virtualMachine, classManager)
  lazy val eventManager = {
    import org.senkbeil.debugger.events.EventType._
    val _eventManager = new EventManager(_virtualMachine, loopingTaskRunner)

    logger.debug(vmString("Adding custom event handlers!"))

    // Mark start event to load all of our system classes
    _eventManager.addResumingEventHandler(VMStartEventType, _ => {
      logger.trace(vmString("Refreshing all class references!"))
      classManager.refreshAllClasses()

      logger.trace(vmString("Applying any pending breakpoints for references!"))
      classManager.allFileNames
        .foreach(breakpointManager.processPendingBreakpoints)
    })

    // Mark class prepare events to signal refreshing our classes
    _eventManager.addResumingEventHandler(ClassPrepareEventType, e => {
      val classPrepareEvent = e.asInstanceOf[ClassPrepareEvent]
      val referenceType = classPrepareEvent.referenceType()
      val referenceTypeName = referenceType.name()
      val fileName = classManager.fileNameForReferenceType(referenceType)

      logger.trace(vmString(s"Received new class: $referenceTypeName"))
      classManager.refreshClass(referenceType)

      logger.trace(vmString(
        s"Processing any pending breakpoints for $referenceTypeName!"))
      breakpointManager.processPendingBreakpoints(fileName)
    })

    _eventManager
  }

  /**
   * Represents the underlying virtual machine represented by this Scala
   * virtual machine.
   *
   * @return The JDI VirtualMachine instance
   */
  val underlyingVirtualMachine: VirtualMachine = _virtualMachine

  /**
   * Retrieves the list of available lines for a specific file.
   *
   * @param fileName The name of the file whose lines to retrieve
   *
   * @return Some list of breakpointable lines if the file exists, otherwise
   *         None
   */
  def availableLinesForFile(fileName: String): Option[Seq[Int]] =
    classManager.linesAndLocationsForFile(fileName).map(_.keys.toSeq.sorted)

  /**
   * Represents the fully-qualified class name that invoked the main method of
   * this virtual machine.
   *
   * @return The name as a string
   */
  lazy val mainClassName: String = {
    val mainThread = findMainThread().get

    // TODO: Investigate if necessary to suspend entire virtual machine or
    //       just the main thread or not at all
    val tryClassName = suspendVirtualMachineAndExecute {
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
   * Represents the command line arguments used to start this virtual machine.
   *
   * @return The sequence of arguments as strings
   */
  lazy val commandLineArguments: Seq[String] = {
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
          logger.warn(vmString(s"Unknown value while processing arguments: $v"))
          Nil
      }
    }

    // Get the main thread of execution
    val mainThread = findMainThread().get

    // TODO: Investigate if necessary to suspend entire virtual machine or
    //       just the main thread or not at all
    // Retrieve command line arguments for connected JVM
    val tryArguments = suspendVirtualMachineAndExecute {
      suspendThreadAndExecute(mainThread) {
        val arguments = mainThread.frames().asScala
          .find(_.location().method().name() == "main")
          .map(_.getArgumentValues.asScala.toSeq)
          .map(processArguments)

        assert(arguments.nonEmpty, "Error locating main method!")

        arguments.get
      }
    }.flatten

    // Throw our exception if we get one
    tryArguments.failed.foreach(ex => throw ex)

    // Return the resulting arguments
    tryArguments.get
  }
}

