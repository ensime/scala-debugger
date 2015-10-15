package org.senkbeil.debugger.api.breakpoints

import com.sun.jdi.request.BreakpointRequest
import com.sun.jdi.{ObjectReference, ThreadReference, VirtualMachine}

/**
 * Represents a collection of breakpoint requests.
 *
 * @param breakpointRequests The initial collection of breakpoint requests
 */
class BreakpointBundle(
    private var breakpointRequests: Seq[BreakpointRequest] = Nil
) extends Seq[BreakpointRequest] {
  /**
   * Returns the breakpoint at the specified index.
   *
   * @param idx The index of the breakpoint to retrieve
   *
   * @return The breakpoint request at the index
   */
  override def apply(idx: Int): BreakpointRequest = breakpointRequests(idx)

  /**
   * Represents the total number of breakpoints contained in this bundle.
   *
   * @return The total breakpoint requests
   */
  override def length: Int = breakpointRequests.length

  /**
   * Represents an iterator over all breakpoints represented by this bundle.
   *
   * @return The iterator to the sequence of breakpoint requests
   */
  override def iterator: Iterator[BreakpointRequest] =
    breakpointRequests.iterator

  /**
   * Adds an instance filter to all breakpoints contained in this bundle.
   *
   * @param objectReference The object reference to use for the instance filter
   */
  def addInstanceFilter(objectReference: ObjectReference): Unit =
    breakpointRequests.foreach(_.addInstanceFilter(objectReference))

  /**
   * Adds a thread filter to all breakpoints contained in this bundle.
   *
   * @param threadReference The thread reference to use for the thread filter
   */
  def addThreadFilter(threadReference: ThreadReference): Unit =
    breakpointRequests.foreach(_.addThreadFilter(threadReference))

  /**
   * Adds a count filter to all breakpoints contained in this bundle.
   *
   * @param i The number to use for the count filter
   */
  def addCountFilter(i: Int): Unit =
    breakpointRequests.foreach(_.addCountFilter(i))

  /**
   * Disables all breakpoints contained in this bundle.
   */
  def disable(): Unit = breakpointRequests.foreach(_.disable())

  /**
   * Enables all breakpoints contained in this bundle.
   */
  def enable(): Unit = breakpointRequests.foreach(_.enable())

  /**
   * Retrieves the property contained by all underlying breakpoints.
   *
   * @param o The property to retrieve
   *
   * @throws AssertionError If the breakpoints are out of sync
   *
   * @return The property value
   */
  @throws(classOf[AssertionError])
  def getProperty(o: scala.Any): AnyRef = {
    val properties = breakpointRequests.map(_.getProperty(o))
    val globalProperty = properties.head

    assert(properties.forall(globalProperty == _),
      "Underlying properties are not in sync!")

    globalProperty
  }

  /**
   * Determines whether the underlying breakpoints are enabled or disabled.
   *
   * @throws AssertionError If the breakpoints are out of sync
   *
   * @return True if enabled, otherwise false
   */
  @throws(classOf[AssertionError])
  def isEnabled: Boolean = {
    val isEnabledStatuses = breakpointRequests.map(_.isEnabled)
    val globalIsEnabled = isEnabledStatuses.head

    assert(isEnabledStatuses.forall(globalIsEnabled == _),
      "Underlying breakpoint enabled statuses are not in sync!")

    globalIsEnabled
  }

  /**
   * Sets the property for all underlying breakpoints.
   *
   * @param o The property key
   * @param o1 The property value
   *
   * @throws AssertionError If the breakpoints are out of sync
   */
  @throws(classOf[AssertionError])
  def putProperty(o: scala.Any, o1: scala.Any): Unit =
    breakpointRequests.foreach(_.putProperty(o, o1))

  /**
   * Sets whether or not each underlying breakpoint is enabled.
   *
   * @param b Whether or not the breakpoints are enabled
   */
  def setEnabled(b: Boolean): Unit =
    breakpointRequests.foreach(_.setEnabled(b))

  /**
   * Sets the suspend policy for all underlying breakpoints.
   *
   * @param i The suspend policy for all breakpoints
   */
  def setSuspendPolicy(i: Int): Unit =
    breakpointRequests.foreach(_.setSuspendPolicy(i))

  /**
   * Retrieves the suspend policy for all underlying breakpoints.
   *
   * @throws AssertionError If the breakpoints are out of sync
   *
   * @return The suspend policy for the collection of breakpoints
   */
  @throws(classOf[AssertionError])
  def suspendPolicy: Int = {
    val suspendPolicies = breakpointRequests.map(_.suspendPolicy())
    val globalSuspendPolicy = suspendPolicies.head

    assert(suspendPolicies.forall(globalSuspendPolicy == _),
      "Underlying suspend policies are not in sync!")

    globalSuspendPolicy
  }

  /**
   * Retrieves the virtual machine for all underlying breakpoints.
   *
   * @throws AssertionError If the breakpoints are out of sync
   *
   * @return The virtual machine containing the collection of breakpoints
   */
  @throws(classOf[AssertionError])
  def virtualMachine: VirtualMachine = {
    val virtualMachines = breakpointRequests.map(_.virtualMachine())
    val globalVirtualMachine = virtualMachines.head

    assert(virtualMachines.forall(globalVirtualMachine eq _),
      "Underlying virtual machines are not in sync!")

    globalVirtualMachine
  }

}
