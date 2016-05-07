package test
import acyclic.file

import com.sun.jdi.request.MethodExitRequest
import org.scaladebugger.api.lowlevel.methods.{MethodExitManager, MethodExitRequestInfo}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Test method exit manager that merely invokes the provided method exit
 * manager underneath to make it easier to mock.
 *
 * @param methodExitManager The underlying method exit manager used to
 *                           execute all methods
 */
class TestMethodExitManager(
  private val methodExitManager: MethodExitManager
) extends MethodExitManager {
  override def methodExitRequestListById: Seq[String] =
    methodExitManager.methodExitRequestListById
  override def createMethodExitRequestWithId(requestId: String, className: String, methodName: String, extraArguments: JDIRequestArgument*): Try[String] =
    methodExitManager.createMethodExitRequestWithId(requestId, className, methodName, extraArguments: _*)
  override def getMethodExitRequest(className: String, methodName: String): Option[Seq[MethodExitRequest]] =
    methodExitManager.getMethodExitRequest(className, methodName)
  override def methodExitRequestList: Seq[MethodExitRequestInfo] =
    methodExitManager.methodExitRequestList
  override def removeMethodExitRequest(className: String, methodName: String): Boolean =
    methodExitManager.removeMethodExitRequest(className, methodName)
  override def removeMethodExitRequestWithId(requestId: String): Boolean =
    methodExitManager.removeMethodExitRequestWithId(requestId)
  override def hasMethodExitRequest(className: String, methodName: String): Boolean =
    methodExitManager.hasMethodExitRequest(className, methodName)
  override def hasMethodExitRequestWithId(requestId: String): Boolean =
    methodExitManager.hasMethodExitRequestWithId(requestId)
  override def getMethodExitRequestWithId(requestId: String): Option[MethodExitRequest] =
    methodExitManager.getMethodExitRequestWithId(requestId)
  override def getMethodExitRequestInfoWithId(requestId: String): Option[MethodExitRequestInfo] =
    methodExitManager.getMethodExitRequestInfoWithId(requestId)
}
