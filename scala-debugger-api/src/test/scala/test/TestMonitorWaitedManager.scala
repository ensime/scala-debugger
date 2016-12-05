package test

import com.sun.jdi.request.MonitorWaitedRequest
import org.scaladebugger.api.lowlevel.monitors.{MonitorWaitedRequestInfo, MonitorWaitedManager}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Test monitor waited manager that merely invokes the provided
 * monitor waited manager underneath to make it easier to mock.
 *
 * @param monitorWaitedManager The underlying monitor waited manager used to
 *                             execute all methods
 */
class TestMonitorWaitedManager(
  private val monitorWaitedManager: MonitorWaitedManager
) extends MonitorWaitedManager {
  override def monitorWaitedRequestList: Seq[String] =
    monitorWaitedManager.monitorWaitedRequestList
  override def hasMonitorWaitedRequest(id: String): Boolean =
    monitorWaitedManager.hasMonitorWaitedRequest(id)
  override def getMonitorWaitedRequest(id: String): Option[MonitorWaitedRequest] =
    monitorWaitedManager.getMonitorWaitedRequest(id)
  override def createMonitorWaitedRequestWithId(requestId: String, extraArguments: JDIRequestArgument*): Try[String] =
    monitorWaitedManager.createMonitorWaitedRequestWithId(requestId, extraArguments: _*)
  override def removeMonitorWaitedRequest(id: String): Boolean =
    monitorWaitedManager.removeMonitorWaitedRequest(id)
  override def getMonitorWaitedRequestInfo(id: String): Option[MonitorWaitedRequestInfo] =
    monitorWaitedManager.getMonitorWaitedRequestInfo(id)
}
