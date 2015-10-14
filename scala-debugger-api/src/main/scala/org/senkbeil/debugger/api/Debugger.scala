package org.senkbeil.debugger.api

import com.sun.jdi.VirtualMachine
import org.senkbeil.debugger.api.jdi.JDILoader

/**
 * Represents the generic interface that all debugger instances implement.
 */
trait Debugger {
  protected val jdiLoader = new JDILoader(this.getClass.getClassLoader)

  /**
   * Determines whether or not the debugger is available for use.
   *
   * @return True if the debugger is available, otherwise false
   */
  def isAvailable: Boolean = jdiLoader.isJdiAvailable()

  /**
   * Attempts to load the JDI, asserting that it can be and is loaded.
   *
   * @throws AssertionError If failed to load the JDI
   */
  @throws(classOf[AssertionError])
  protected def assertJdiLoaded(): Unit =
    assert(jdiLoader.tryLoadJdi(),
      """
        |Unable to load Java Debugger Interface! This is part of tools.jar
        |provided by OpenJDK/Oracle JDK and is the core of the debugger! Please
        |make sure that JAVA_HOME has been set and that tools.jar is available
        |on the classpath!
      """.stripMargin.replace("\n", " "))

  /**
   * Starts the debugger, performing any necessary setup and ending with
   * an initialized debugger that is or will be capable of connecting to one or
   * more virtual machine instances.
   *
   * @param newVirtualMachineFunc The function that will be called when a new
   *                              virtual machine connection is created as a
   *                              result of this debugger
   * @tparam T The type of return
   */
  def start[T](newVirtualMachineFunc: VirtualMachine => T): Unit

  /**
   * Shuts down the debugger, releasing any connected virtual machines.
   */
  def stop(): Unit

  /**
   * Indicates whether or not the debugger is running.
   *
   * @return True if it is running, otherwise false
   */
  def isRunning: Boolean
}
