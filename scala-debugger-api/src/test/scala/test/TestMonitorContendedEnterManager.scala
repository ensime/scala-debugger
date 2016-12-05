package test

import com.sun.jdi.request.MonitorContendedEnterRequest
import org.scaladebugger.api.lowlevel.monitors.{MonitorContendedEnterRequestInfo, MonitorContendedEnterManager}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Test monitor contended enter manager that merely invokes the provided
 * monitor contended enter manager underneath to make it easier to mock.
 *
 * @param monitorContendedEnterManager The underlying monitor contended
 *                                     enter manager used to execute
 *                                     all methods
 */
class TestMonitorContendedEnterManager(
  private val monitorContendedEnterManager: MonitorContendedEnterManager
) extends MonitorContendedEnterManager {
  override def monitorContendedEnterRequestList: Seq[String] =
    monitorContendedEnterManager.monitorContendedEnterRequestList
  override def hasMonitorContendedEnterRequest(id: String): Boolean =
    monitorContendedEnterManager.hasMonitorContendedEnterRequest(id)
  override def getMonitorContendedEnterRequest(id: String): Option[MonitorContendedEnterRequest] =
    monitorContendedEnterManager.getMonitorContendedEnterRequest(id)
  override def createMonitorContendedEnterRequestWithId(requestId: String, extraArguments: JDIRequestArgument*): Try[String] =
    monitorContendedEnterManager.createMonitorContendedEnterRequestWithId(requestId, extraArguments: _*)
  override def removeMonitorContendedEnterRequest(id: String): Boolean =
    monitorContendedEnterManager.removeMonitorContendedEnterRequest(id)
  override def getMonitorContendedEnterRequestInfo(id: String): Option[MonitorContendedEnterRequestInfo] =
    monitorContendedEnterManager.getMonitorContendedEnterRequestInfo(id)
}
