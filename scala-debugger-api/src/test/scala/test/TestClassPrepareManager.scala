package test

import com.sun.jdi.request.ClassPrepareRequest
import org.scaladebugger.api.lowlevel.classes.{ClassPrepareRequestInfo, ClassPrepareManager}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Test class prepare manager that merely invokes the provided
 * class prepare manager underneath to make it easier to mock.
 *
 * @param classPrepareManager The underlying class prepare manager used to
 *                            execute all methods
 */
class TestClassPrepareManager(
  private val classPrepareManager: ClassPrepareManager
) extends ClassPrepareManager {
  override def classPrepareRequestList: Seq[String] =
    classPrepareManager.classPrepareRequestList
  override def hasClassPrepareRequest(id: String): Boolean =
    classPrepareManager.hasClassPrepareRequest(id)
  override def getClassPrepareRequest(id: String): Option[ClassPrepareRequest] =
    classPrepareManager.getClassPrepareRequest(id)
  override def createClassPrepareRequestWithId(requestId: String, extraArguments: JDIRequestArgument*): Try[String] =
    classPrepareManager.createClassPrepareRequestWithId(requestId, extraArguments: _*)
  override def removeClassPrepareRequest(id: String): Boolean =
    classPrepareManager.removeClassPrepareRequest(id)
  override def getClassPrepareRequestInfo(id: String): Option[ClassPrepareRequestInfo] =
    classPrepareManager.getClassPrepareRequestInfo(id)
}
