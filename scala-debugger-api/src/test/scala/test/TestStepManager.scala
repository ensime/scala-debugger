package test

import com.sun.jdi.ThreadReference
import com.sun.jdi.request.StepRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.steps.{StepRequestInfo, StepManager}

import scala.util.Try

/**
 * Test step manager that merely invokes the provided step manager
 * underneath to make it easier to mock.
 *
 * @param stepManager The underlying step manager used to execute all methods
 */
class TestStepManager(
  private val stepManager: StepManager
) extends StepManager {
  override def getStepRequestInfoWithId(requestId: String): Option[StepRequestInfo] =
    stepManager.getStepRequestInfoWithId(requestId)
  override def hasStepRequestWithId(requestId: String): Boolean =
    stepManager.hasStepRequestWithId(requestId)
  override def hasStepRequest(threadReference: ThreadReference): Boolean =
    stepManager.hasStepRequest(threadReference)
  override def stepRequestListById: Seq[String] =
    stepManager.stepRequestListById
  override def getStepRequest(threadReference: ThreadReference): Option[Seq[StepRequest]] =
    stepManager.getStepRequest(threadReference)
  override def createStepRequestWithId(requestId: String, removeExistingRequests: Boolean, threadReference: ThreadReference, size: Int, depth: Int, extraArguments: JDIRequestArgument*): Try[String] =
    stepManager.createStepRequestWithId(requestId, removeExistingRequests, threadReference, size, depth, extraArguments: _*)
  override def getStepRequestWithId(requestId: String): Option[StepRequest] =
    stepManager.getStepRequestWithId(requestId)
  override def removeStepRequest(threadReference: ThreadReference): Boolean =
    stepManager.removeStepRequest(threadReference)
  override def stepRequestList: Seq[StepRequestInfo] =
    stepManager.stepRequestList
  override def removeStepRequestWithId(requestId: String): Boolean =
    stepManager.removeStepRequestWithId(requestId)
}
