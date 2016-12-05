package test

import com.sun.jdi.request.AccessWatchpointRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.watchpoints.{AccessWatchpointRequestInfo, AccessWatchpointManager}

import scala.util.Try

/**
 * Test access watchpoint manager that merely invokes the provided access
 * watchpoint manager underneath to make it easier to mock.
 *
 * @param accessWatchpointManager The underlying access watchpoint manager used
 *                                to execute all methods
 */
class TestAccessWatchpointManager(
  private val accessWatchpointManager: AccessWatchpointManager
) extends AccessWatchpointManager {
  override def accessWatchpointRequestList: Seq[AccessWatchpointRequestInfo] =
    accessWatchpointManager.accessWatchpointRequestList
  override def hasAccessWatchpointRequestWithId(id: String): Boolean =
    accessWatchpointManager.hasAccessWatchpointRequestWithId(id)
  override def removeAccessWatchpointRequest(className: String, fieldName: String): Boolean =
    accessWatchpointManager.removeAccessWatchpointRequest(className, fieldName)
  override def createAccessWatchpointRequestWithId(requestId: String, className: String, fieldName: String, extraArguments: JDIRequestArgument*): Try[String] =
    accessWatchpointManager.createAccessWatchpointRequestWithId(requestId, className, fieldName, extraArguments: _*)
  override def getAccessWatchpointRequest(className: String, fieldName: String): Option[Seq[AccessWatchpointRequest]] =
    accessWatchpointManager.getAccessWatchpointRequest(className, fieldName)
  override def getAccessWatchpointRequestWithId(id: String): Option[AccessWatchpointRequest] =
    accessWatchpointManager.getAccessWatchpointRequestWithId(id)
  override def getAccessWatchpointRequestInfoWithId(requestId: String): Option[AccessWatchpointRequestInfo] =
    accessWatchpointManager.getAccessWatchpointRequestInfoWithId(requestId)
  override def accessWatchpointRequestListById: Seq[String] =
    accessWatchpointManager.accessWatchpointRequestListById
  override def removeAccessWatchpointRequestWithId(id: String): Boolean =
    accessWatchpointManager.removeAccessWatchpointRequestWithId(id)
  override def hasAccessWatchpointRequest(className: String, fieldName: String): Boolean =
    accessWatchpointManager.hasAccessWatchpointRequest(className, fieldName)
}
