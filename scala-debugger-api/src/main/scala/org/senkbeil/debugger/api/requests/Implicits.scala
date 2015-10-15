package org.senkbeil.debugger.api.requests

import com.sun.jdi.request.EventRequestManager

/**
 * Contains request-related implicits.
 */
object Implicits {
  import scala.language.implicitConversions

  implicit def eventRequestManagerToEventRequestManagerWrapper(
    eventRequestManager: EventRequestManager
  ): EventRequestManagerWrapper =
    new EventRequestManagerWrapper(eventRequestManager)
}
