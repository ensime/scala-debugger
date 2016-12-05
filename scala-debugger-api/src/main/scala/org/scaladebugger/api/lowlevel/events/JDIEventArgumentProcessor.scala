package org.scaladebugger.api.lowlevel.events

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.data.{JDIEventDataProcessor, JDIEventDataResult, JDIEventDataUnknownError}
import org.scaladebugger.api.lowlevel.events.filters.JDIEventFilterProcessor

import scala.util.Try

/**
 * Represents a processor for arguments for JDI Events. Evaluates the filters
 * in order, short-circuiting if a filter denies the event.
 *
 * @param arguments The collection of arguments to use
 */
class JDIEventArgumentProcessor(private val arguments: JDIEventArgument*) {
  /** Contains a collection of processors based on provided arguments. */
  private val processors = arguments.map(_.toProcessor)

  /** Contains only filter-oriented processors. */
  private val filterProcessors = processors
    .groupBy(_.isInstanceOf[JDIEventFilterProcessor])
    .getOrElse(true, Nil)
    .map(_.asInstanceOf[JDIEventFilterProcessor])

  /** Contains only data-oriented processors. */
  private val dataProcessors = processors
    .groupBy(_.isInstanceOf[JDIEventDataProcessor])
    .getOrElse(true, Nil)
    .map(_.asInstanceOf[JDIEventDataProcessor])

  /** Contains all other processors other than filter and data. */
  private val otherProcessors = processors
    .diff(filterProcessors)
    .diff(dataProcessors)

  /**
   * Processes the event, applying all provided event arguments.
   *
   * @param event The event to process
   * @param forceAllArguments If true, forces all arguments to be evaluated,
   *                        regardless of whether an earlier argument denies
   *                        the event
   *
   * @return (process result, optional request data, optional other data) in
   *         the form: true if the event passes all of the arguments,
   *         otherwise false
   */
  def processAll(
    event: Event,
    forceAllArguments: Boolean = false
  ): (Boolean, Seq[JDIEventDataResult], Seq[Any]) = {
    val filterResult = processFilters(event, forceAllArguments)
    val dataResult = if (filterResult) processData(event) else Nil
    val otherResult = if (filterResult) processOther(event) else Nil

    (filterResult, dataResult, otherResult)
  }

  /**
   * Processes the event for each filter.
   *
   * @param event The event to process
   * @param forceAllFilters If true, forces all filters to be executed,
   *                        otherwise short-circuits if one returns false
   *
   * @return True if all filter processors return true, otherwise false
   */
  def processFilters(
    event: Event,
    forceAllFilters: Boolean = false
  ): Boolean = {
    // Evaluate all filters and determine if all succeed
    if (forceAllFilters)
      filterProcessors.map(_.process(event)).forall(_ == true)

    // Evaluate filters until one is found that fails
    else
      filterProcessors.find(_.process(event) == false).isEmpty
  }

  /**
   * Processes the event for each data request.
   *
   * @param event The event to process
   *
   * @return The data results and errors from the data processors
   */
  def processData(event: Event): Seq[JDIEventDataResult] = {
    val results = dataProcessors.map(p => Try(p.process(event)))

    results.flatMap(_.toOption).flatten ++
      results.flatMap(_.failed.toOption).map(JDIEventDataUnknownError.apply)
  }

  /**
   * Processes the event for all other arguments.
   *
   * @param event The event to process
   *
   * @return The arbitrary results from the other processors
   */
  def processOther(event: Event): Seq[Any] = {
    otherProcessors.map(_.process(event))
  }
}
