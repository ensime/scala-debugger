package org.scaladebugger.api.lowlevel

import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

case class StandardRequestInfo(
  requestId: String,
  isPending: Boolean,
  extraArguments: Seq[JDIRequestArgument]
) extends RequestInfo
