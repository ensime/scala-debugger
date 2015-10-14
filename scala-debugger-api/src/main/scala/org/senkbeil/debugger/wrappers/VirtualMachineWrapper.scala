package org.senkbeil.debugger.wrappers

import java.util.concurrent.ConcurrentHashMap

import org.senkbeil.debugger.utils.LogLike
import com.sun.jdi.request.EventRequest
import com.sun.jdi.VirtualMachine

import scala.collection.JavaConverters._

/**
 * Represents a wrapper around a virtual machine, providing additional methods.
 *
 * @param _virtualMachine The virtual machine to wrap
 */
class VirtualMachineWrapper(private val _virtualMachine: VirtualMachine)
  extends LogLike
{
  require(_virtualMachine != null, "Virtual machine cannot be null!")

  /** Represents the event request manager used by the underlying vm. */
  private val eventRequestManager = _virtualMachine.eventRequestManager()

  /** Contains all requests except for breakpoint and step. */
  private val requestMap: collection.mutable.Map[String, EventRequest] =
    new ConcurrentHashMap[String, EventRequest]().asScala

  /** The id of the current request for class preparation events. */
  @volatile private var classPrepareRequestId: Option[String] = None

  /** The id of the current request for thread start events. */
  @volatile private var threadStartRequestId: Option[String] = None

  /** The id of the current request for thread death events. */
  @volatile private var threadDeathRequestId: Option[String] = None

  /** The id of the current request for exception events. */
  @volatile private var exceptionRequestId: Option[String] = None

  /**
   * Sets various properties on the provided request and stores it.
   *
   * @param eventRequest The request whose properties to set
   * @param suspendPolicy The suspend policy to set on the request
   * @param enable Whether or not to enable the request
   * @return The unique id for the request
   */
  private def populateRequest(
    eventRequest: EventRequest,
    suspendPolicy: Int,
    enable: Boolean
  ): String = {
    val id = java.util.UUID.randomUUID().toString

    eventRequest.setSuspendPolicy(suspendPolicy)
    if (enable) eventRequest.enable()

    requestMap.put(id, eventRequest)

    id
  }

  /**
   * Removes the request with the specified id.
   *
   * @param id The id of the request to remove
   */
  private def removeRequest(id: String): Unit = {
    val request = requestMap.remove(id)
    request.foreach(eventRequestManager.deleteEventRequest)
  }

  /**
   * Enables class prepare events (no filters) for this virtual machine.
   *
   * @param suspendPolicy The suspend policy to use (defaults to all threads)
   *
   * @throws AssertionError If already enabled
   */
  @throws(classOf[AssertionError])
  def enableClassPrepareEvents(
    suspendPolicy: Int = EventRequest.SUSPEND_ALL
  ): Unit = {
    assert(classPrepareRequestId.isEmpty,
      "Class prepare events already enabled!")

    classPrepareRequestId = Some(populateRequest(
      eventRequest  = eventRequestManager.createClassPrepareRequest(),
      suspendPolicy = suspendPolicy,
      enable        = true
    ))
  }

  /**
   * Disables class prepare events for this virtual machine.
   *
   * @throws AssertionError If not enabled
   */
  @throws(classOf[AssertionError])
  def disableClassPrepareEvents(): Unit = {
    assert(classPrepareRequestId.nonEmpty, "Class prepare events not enabled!")

    classPrepareRequestId.foreach(removeRequest)
  }

  /**
   * Enables thread start events (no filters) for this virtual machine.
   *
   * @param suspendPolicy The suspend policy to use (defaults to none)
   *
   * @throws AssertionError If already enabled
   */
  @throws(classOf[AssertionError])
  def enableThreadStartEvents(
    suspendPolicy: Int = EventRequest.SUSPEND_NONE
  ): Unit = {
    assert(threadStartRequestId.isEmpty, "Thread start events already enabled!")

    threadStartRequestId = Some(populateRequest(
      eventRequest  = eventRequestManager.createThreadStartRequest(),
      suspendPolicy = suspendPolicy,
      enable        = true
    ))
  }

  /**
   * Disables thread start events for this virtual machine.
   *
   * @throws AssertionError If not enabled
   */
  @throws(classOf[AssertionError])
  def disableThreadStartEvents(): Unit = {
    assert(threadStartRequestId.nonEmpty, "Thread start events not enabled!")

    threadStartRequestId.foreach(removeRequest)
  }

  /**
   * Enables thread death events (no filters) for this virtual machine.
   *
   * @param suspendPolicy The suspend policy to use (defaults to none)
   *
   * @throws AssertionError If already enabled
   */
  @throws(classOf[AssertionError])
  def enableThreadDeathEvents(
    suspendPolicy: Int = EventRequest.SUSPEND_NONE
  ): Unit = {
    assert(threadDeathRequestId.isEmpty, "Thread death events already enabled!")

    threadDeathRequestId = Some(populateRequest(
      eventRequest  = eventRequestManager.createThreadDeathRequest(),
      suspendPolicy = suspendPolicy,
      enable        = true
    ))
  }

  /**
   * Disables thread death events for this virtual machine.
   *
   * @throws AssertionError If not enabled
   */
  @throws(classOf[AssertionError])
  def disableThreadDeathEvents(): Unit = {
    assert(threadDeathRequestId.nonEmpty, "Thread death events not enabled!")

    threadDeathRequestId.foreach(removeRequest)
  }

  /**
   * Enables exception events (no filters) for this virtual machine.
   *
   * @param suspendPolicy The suspend policy to use (defaults to none)
   *
   * @throws AssertionError If already enabled
   */
  @throws(classOf[AssertionError])
  def enableExceptionEvents(
    suspendPolicy: Int = EventRequest.SUSPEND_ALL
  ): Unit = {
    assert(exceptionRequestId.isEmpty, "Exception events already enabled!")

    exceptionRequestId = Some(populateRequest(
      eventRequest  = eventRequestManager.createExceptionRequest(
        null, false, true
      ),
      suspendPolicy = suspendPolicy,
      enable        = true
    ))
  }

  /**
   * Disables exception events for this virtual machine.
   *
   * @throws AssertionError If not enabled
   */
  @throws(classOf[AssertionError])
  def disableExceptionEvents(): Unit = {
    assert(exceptionRequestId.nonEmpty, "Exception events not enabled!")

    exceptionRequestId.foreach(removeRequest)
  }
}
