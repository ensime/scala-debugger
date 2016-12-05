package test

import com.sun.jdi.request.ModificationWatchpointRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.watchpoints.{ModificationWatchpointManager, ModificationWatchpointRequestInfo}

import scala.util.Try
/**
 * Test modification watchpoint manager that merely invokes the provided
 * modification watchpoint manager underneath to make it easier to mock.
 *
 * @param modificationWatchpointManager The underlying modification
 *                                      watchpoint manager used
 *                                      to execute all methods
 */
class TestModificationWatchpointManager(
  private val modificationWatchpointManager: ModificationWatchpointManager
) extends ModificationWatchpointManager {
  override def modificationWatchpointRequestList: Seq[ModificationWatchpointRequestInfo] =
    modificationWatchpointManager.modificationWatchpointRequestList
  override def hasModificationWatchpointRequestWithId(id: String): Boolean =
    modificationWatchpointManager.hasModificationWatchpointRequestWithId(id)
  override def removeModificationWatchpointRequest(className: String, fieldName: String): Boolean =
    modificationWatchpointManager.removeModificationWatchpointRequest(className, fieldName)
  override def createModificationWatchpointRequestWithId(requestId: String, className: String, fieldName: String, extraArguments: JDIRequestArgument*): Try[String] =
    modificationWatchpointManager.createModificationWatchpointRequestWithId(requestId, className, fieldName, extraArguments: _*)
  override def getModificationWatchpointRequest(className: String, fieldName: String): Option[Seq[ModificationWatchpointRequest]] =
    modificationWatchpointManager.getModificationWatchpointRequest(className, fieldName)
  override def getModificationWatchpointRequestWithId(id: String): Option[ModificationWatchpointRequest] =
    modificationWatchpointManager.getModificationWatchpointRequestWithId(id)
  override def getModificationWatchpointRequestInfoWithId(requestId: String): Option[ModificationWatchpointRequestInfo] =
    modificationWatchpointManager.getModificationWatchpointRequestInfoWithId(requestId)
  override def modificationWatchpointRequestListById: Seq[String] =
    modificationWatchpointManager.modificationWatchpointRequestListById
  override def removeModificationWatchpointRequestWithId(id: String): Boolean =
    modificationWatchpointManager.removeModificationWatchpointRequestWithId(id)
  override def hasModificationWatchpointRequest(className: String, fieldName: String): Boolean =
    modificationWatchpointManager.hasModificationWatchpointRequest(className, fieldName)
}

