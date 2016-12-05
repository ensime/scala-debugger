package org.scaladebugger.api.lowlevel.utils

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents a collection of JDI arguments grouped based on whether they are
 * for requests, events, or something else.
 *
 * @param requestArguments All JDI arguments for requests
 * @param eventArguments All JDI arguments for events
 * @param otherArguments Any other arguments that serve another purpose
 */
case class JDIArgumentGroup(
  requestArguments: Seq[JDIRequestArgument],
  eventArguments: Seq[JDIEventArgument],
  otherArguments: Seq[AnyRef]
)

/**
 * Provides helper methods to construct an argument group.
 */
object JDIArgumentGroup {
  /**
   * Constructs a new JDI argument group based on the provided arguments.
   *
   * @param jdiArguments The collection of arguments to form into groups
   *
   * @return The resulting collection of request, event, and other grouped
   *         JDI arguments
   */
  def apply(jdiArguments: JDIArgument*): JDIArgumentGroup = {
    var jdiRequestArguments: Seq[JDIRequestArgument] = Nil
    var jdiEventArguments: Seq[JDIEventArgument] = Nil
    var jdiOtherArguments: Seq[AnyRef] = Nil

    jdiArguments.foreach {
      case arg: JDIRequestArgument  => jdiRequestArguments :+= arg
      case arg: JDIEventArgument    => jdiEventArguments :+= arg
      case arg                      => jdiOtherArguments :+= arg
    }

    JDIArgumentGroup(
      jdiRequestArguments,
      jdiEventArguments,
      jdiOtherArguments
    )
  }
}
