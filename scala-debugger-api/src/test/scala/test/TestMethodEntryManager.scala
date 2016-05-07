package test
import acyclic.file

import com.sun.jdi.request.MethodEntryRequest
import org.scaladebugger.api.lowlevel.methods.{MethodEntryRequestInfo, MethodEntryManager}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Test method entry manager that merely invokes the provided method entry
 * manager underneath to make it easier to mock.
 *
 * @param methodEntryManager The underlying method entry manager used to
 *                           execute all methods
 */
class TestMethodEntryManager(
  private val methodEntryManager: MethodEntryManager
) extends MethodEntryManager {
  override def methodEntryRequestListById: Seq[String] =
    methodEntryManager.methodEntryRequestListById
  override def createMethodEntryRequestWithId(requestId: String, className: String, methodName: String, extraArguments: JDIRequestArgument*): Try[String] =
    methodEntryManager.createMethodEntryRequestWithId(requestId, className, methodName, extraArguments: _*)
  override def getMethodEntryRequest(className: String, methodName: String): Option[Seq[MethodEntryRequest]] =
    methodEntryManager.getMethodEntryRequest(className, methodName)
  override def getMethodEntryRequestInfoWithId(requestId: String): Option[MethodEntryRequestInfo] =
    methodEntryManager.getMethodEntryRequestInfoWithId(requestId)
  override def methodEntryRequestList: Seq[MethodEntryRequestInfo] =
    methodEntryManager.methodEntryRequestList
  override def removeMethodEntryRequest(className: String, methodName: String): Boolean =
    methodEntryManager.removeMethodEntryRequest(className, methodName)
  override def removeMethodEntryRequestWithId(requestId: String): Boolean =
    methodEntryManager.removeMethodEntryRequestWithId(requestId)
  override def hasMethodEntryRequest(className: String, methodName: String): Boolean =
    methodEntryManager.hasMethodEntryRequest(className, methodName)
  override def hasMethodEntryRequestWithId(requestId: String): Boolean =
    methodEntryManager.hasMethodEntryRequestWithId(requestId)
  override def getMethodEntryRequestWithId(requestId: String): Option[MethodEntryRequest] =
    methodEntryManager.getMethodEntryRequestWithId(requestId)
}
