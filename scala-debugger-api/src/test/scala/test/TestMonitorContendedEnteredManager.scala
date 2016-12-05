package test

import com.sun.jdi.request.MonitorContendedEnteredRequest
import org.scaladebugger.api.lowlevel.monitors.{MonitorContendedEnteredRequestInfo, MonitorContendedEnteredManager}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Test monitor contended entered manager that merely invokes the provided
 * monitor contended entered manager underneath to make it easier to mock.
 *
 * @param monitorContendedEnteredManager The underlying monitor contended
 *                                       entered manager used to execute
 *                                       all methods
 */
class TestMonitorContendedEnteredManager(
  private val monitorContendedEnteredManager: MonitorContendedEnteredManager
) extends MonitorContendedEnteredManager {
  override def monitorContendedEnteredRequestList: Seq[String] =
    monitorContendedEnteredManager.monitorContendedEnteredRequestList
  override def hasMonitorContendedEnteredRequest(id: String): Boolean =
    monitorContendedEnteredManager.hasMonitorContendedEnteredRequest(id)
  override def getMonitorContendedEnteredRequest(id: String): Option[MonitorContendedEnteredRequest] =
    monitorContendedEnteredManager.getMonitorContendedEnteredRequest(id)
  override def createMonitorContendedEnteredRequestWithId(requestId: String, extraArguments: JDIRequestArgument*): Try[String] =
    monitorContendedEnteredManager.createMonitorContendedEnteredRequestWithId(requestId, extraArguments: _*)
  override def removeMonitorContendedEnteredRequest(id: String): Boolean =
    monitorContendedEnteredManager.removeMonitorContendedEnteredRequest(id)
  override def getMonitorContendedEnteredRequestInfo(id: String): Option[MonitorContendedEnteredRequestInfo] =
    monitorContendedEnteredManager.getMonitorContendedEnteredRequestInfo(id)
}
