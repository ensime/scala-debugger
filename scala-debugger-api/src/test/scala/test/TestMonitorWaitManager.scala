package test

import com.sun.jdi.request.MonitorWaitRequest
import org.scaladebugger.api.lowlevel.monitors.{MonitorWaitRequestInfo, MonitorWaitManager}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Test monitor wait manager that merely invokes the provided
 * monitor wait manager underneath to make it easier to mock.
 *
 * @param monitorWaitManager The underlying monitor wait manager used to
 *                           execute all methods
 */
class TestMonitorWaitManager(
  private val monitorWaitManager: MonitorWaitManager
) extends MonitorWaitManager {
  override def monitorWaitRequestList: Seq[String] =
    monitorWaitManager.monitorWaitRequestList
  override def hasMonitorWaitRequest(id: String): Boolean =
    monitorWaitManager.hasMonitorWaitRequest(id)
  override def getMonitorWaitRequest(id: String): Option[MonitorWaitRequest] =
    monitorWaitManager.getMonitorWaitRequest(id)
  override def createMonitorWaitRequestWithId(requestId: String, extraArguments: JDIRequestArgument*): Try[String] =
    monitorWaitManager.createMonitorWaitRequestWithId(requestId, extraArguments: _*)
  override def removeMonitorWaitRequest(id: String): Boolean =
    monitorWaitManager.removeMonitorWaitRequest(id)
  override def getMonitorWaitRequestInfo(id: String): Option[MonitorWaitRequestInfo] =
    monitorWaitManager.getMonitorWaitRequestInfo(id)
}
