package org.scaladebugger.api.lowlevel
import acyclic.file
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents generic information about a request.
 */
trait RequestInfo {
  /** The id of the request. */
  val requestId: String

  /** Whether or not this request is pending (not on remote JVM). */
  val isPending: Boolean

  /** Represents extra arguments provided to the request. */
  val extraArguments: Seq[JDIRequestArgument]
}
