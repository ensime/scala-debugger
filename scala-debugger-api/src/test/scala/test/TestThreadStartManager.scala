package test

import com.sun.jdi.request.ThreadStartRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.threads.{ThreadStartManager, ThreadStartRequestInfo}

import scala.util.Try

/**
 * Test thread start manager that merely invokes the provided
 * thread start manager underneath to make it easier to mock.
 *
 * @param threadStartManager The underlying thread start manager used to
 *                            execute all methods
 */
class TestThreadStartManager(
  private val threadStartManager: ThreadStartManager
) extends ThreadStartManager {
  override def threadStartRequestList: Seq[String] =
    threadStartManager.threadStartRequestList
  override def hasThreadStartRequest(id: String): Boolean =
    threadStartManager.hasThreadStartRequest(id)
  override def getThreadStartRequest(id: String): Option[ThreadStartRequest] =
    threadStartManager.getThreadStartRequest(id)
  override def createThreadStartRequestWithId(requestId: String, extraArguments: JDIRequestArgument*): Try[String] =
    threadStartManager.createThreadStartRequestWithId(requestId, extraArguments: _*)
  override def removeThreadStartRequest(id: String): Boolean =
    threadStartManager.removeThreadStartRequest(id)
  override def getThreadStartRequestInfo(id: String): Option[ThreadStartRequestInfo] =
    threadStartManager.getThreadStartRequestInfo(id)
}
