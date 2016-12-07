package org.scaladebugger.api.profiles.traits.requests.exceptions
import com.sun.jdi.event.ExceptionEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.exceptions.ExceptionRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ExceptionEventInfo
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.{Failure, Success, Try}

class ExceptionRequestSpec extends FunSpec with Matchers with ParallelTestExecution
  with MockFactory
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[ExceptionRequest#ExceptionEventAndData]
  )

  private val successExceptionProfile = new Object with ExceptionRequest {
    override def tryGetOrCreateExceptionRequestWithData(
      exceptionName: String,
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ExceptionEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def tryGetOrCreateAllExceptionsRequestWithData(
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ExceptionEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def removeOnlyAllExceptionsRequests(): Seq[ExceptionRequestInfo] = ???
    override def removeExceptionRequests(
      exceptionName: String
    ): Seq[ExceptionRequestInfo] = ???
    override def removeAllExceptionRequests(): Seq[ExceptionRequestInfo] = ???
    override def removeExceptionRequestWithArgs(
      exceptionName: String,
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIArgument*
    ): Option[ExceptionRequestInfo] = ???
    override def removeOnlyAllExceptionsRequestWithArgs(
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIArgument*
    ): Option[ExceptionRequestInfo] = ???
    override def isAllExceptionsRequestPending: Boolean = ???
    override def isExceptionRequestWithArgsPending(
      exceptionName: String,
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIArgument*
    ): Boolean = ???
    override def isExceptionRequestPending(exceptionName: String): Boolean = ???
    override def isAllExceptionsRequestWithArgsPending(
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIArgument*
    ): Boolean = ???
    override def exceptionRequests: Seq[ExceptionRequestInfo] = ???
  }

  private val failExceptionProfile = new Object with ExceptionRequest {
    override def removeOnlyAllExceptionsRequests(): Seq[ExceptionRequestInfo] = ???
    override def removeExceptionRequests(
      exceptionName: String
    ): Seq[ExceptionRequestInfo] = ???
    override def removeAllExceptionRequests(): Seq[ExceptionRequestInfo] = ???
    override def removeExceptionRequestWithArgs(
      exceptionName: String,
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIArgument*
    ): Option[ExceptionRequestInfo] = ???
    override def removeOnlyAllExceptionsRequestWithArgs(
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIArgument*
    ): Option[ExceptionRequestInfo] = ???
    override def isAllExceptionsRequestPending: Boolean = ???
    override def isExceptionRequestWithArgsPending(
      exceptionName: String,
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIArgument*
    ): Boolean = ???
    override def isExceptionRequestPending(exceptionName: String): Boolean = ???
    override def isAllExceptionsRequestWithArgsPending(
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIArgument*
    ): Boolean = ???
    override def exceptionRequests: Seq[ExceptionRequestInfo] = ???

    override def tryGetOrCreateExceptionRequestWithData(
      exceptionName: String,
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ExceptionEventAndData]] = {
      Failure(TestThrowable)
    }

    override def tryGetOrCreateAllExceptionsRequestWithData(
      notifyCaught: Boolean,
      notifyUncaught: Boolean,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ExceptionEventAndData]] = {
      Failure(TestThrowable)
    }
  }

  describe("ExceptionRequest") {
    describe("#tryGetOrCreateExceptionRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ExceptionEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ExceptionEventInfo = null
        successExceptionProfile
          .tryGetOrCreateExceptionRequest("", true, true)
          .get
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any error as a failure") {
        val expected = TestThrowable

        var actual: Throwable = null
        failExceptionProfile
          .tryGetOrCreateExceptionRequest("", true, true)
          .failed
          .foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateExceptionRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ExceptionEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ExceptionEventInfo = null
        successExceptionProfile
          .getOrCreateExceptionRequest("", true, true)
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw an error if it occurs") {
        intercept[Throwable] {
          failExceptionProfile.getOrCreateExceptionRequest("", true, true)
        }
      }
    }

    describe("#getOrCreateExceptionRequestWithData") {
      it("should return a pipeline with the event data results") {
        // Data to be run through pipeline
        val expected = (mock[ExceptionEventInfo], Seq(mock[JDIEventDataResult]))

        var actual: (ExceptionEventInfo, Seq[JDIEventDataResult]) = null
        successExceptionProfile
          .getOrCreateExceptionRequestWithData("", true, true)
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw an error if it occurs") {
        intercept[Throwable] {
          failExceptionProfile.getOrCreateExceptionRequestWithData("", true, true)
        }
      }
    }

    describe("#tryGetOrCreateAllExceptionsRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ExceptionEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ExceptionEventInfo = null
        successExceptionProfile
          .tryGetOrCreateAllExceptionsRequest(true, true)
          .get
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any error as a failure") {
        val expected = TestThrowable

        var actual: Throwable = null
        failExceptionProfile
          .tryGetOrCreateAllExceptionsRequest(true, true)
          .failed
          .foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#getOrCreateAllExceptionsRequest") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ExceptionEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ExceptionEventInfo = null
        successExceptionProfile
          .getOrCreateAllExceptionsRequest(true, true)
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw an error if it occurs") {
        intercept[Throwable] {
          failExceptionProfile.getOrCreateAllExceptionsRequest(true, true)
        }
      }
    }

    describe("#getOrCreateAllExceptionsRequestWithData") {
      it("should return a pipeline with the event data results") {
        // Data to be run through pipeline
        val expected = (mock[ExceptionEventInfo], Seq(mock[JDIEventDataResult]))

        var actual: (ExceptionEventInfo, Seq[JDIEventDataResult]) = null
        successExceptionProfile
          .getOrCreateAllExceptionsRequestWithData(true, true)
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw an error if it occurs") {
        intercept[Throwable] {
          failExceptionProfile.getOrCreateAllExceptionsRequestWithData(true, true)
        }
      }
    }
  }
}
