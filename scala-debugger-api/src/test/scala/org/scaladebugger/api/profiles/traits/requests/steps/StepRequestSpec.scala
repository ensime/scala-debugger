package org.scaladebugger.api.profiles.traits.requests.steps
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.steps.StepRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.profiles.traits.info.events.StepEventInfo
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent._
import scala.util.{Failure, Success, Try}

class StepRequestSpec extends ParallelMockFunSpec with ScalaFutures {
  private val mockThreadInfoProfile = mock[ThreadInfo]

  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[StepRequest#StepEventAndData]
  )

  private val successStepProfile = new Object with StepRequest {
    override def stepIntoLineWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Future[StepEventAndData] = ???

    override def stepOverLineWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Future[StepEventAndData] = ???

    override def stepOutLineWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Future[StepEventAndData] = ???

    override def stepIntoMinWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Future[(StepEventAndData)] = ???

    override def stepOutMinWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Future[(StepEventAndData)] = ???

    override def stepOverMinWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Future[(StepEventAndData)] = ???

    override def tryCreateStepListenerWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[StepEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def isStepRequestPending(
      threadInfoProfile: ThreadInfo
    ): Boolean = ???

    override def isStepRequestWithArgsPending(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def stepRequests: Seq[StepRequestInfo] = ???

    override def removeStepRequests(
      threadInfoProfile: ThreadInfo
    ): Seq[StepRequestInfo] = ???

    override def removeStepRequestWithArgs(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Option[StepRequestInfo] = ???

    override def removeAllStepRequests(): Seq[StepRequestInfo] = ???
  }

  private val failStepProfile = new Object with StepRequest {
    override def stepIntoLineWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Future[StepEventAndData] = ???

    override def stepOverLineWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Future[StepEventAndData] = ???

    override def stepOutLineWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Future[StepEventAndData] = ???

    override def stepIntoMinWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Future[(StepEventAndData)] = ???

    override def stepOutMinWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Future[(StepEventAndData)] = ???

    override def stepOverMinWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Future[(StepEventAndData)] = ???

    override def tryCreateStepListenerWithData(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[StepEventAndData]] = {
      Failure(TestThrowable)
    }

    override def isStepRequestPending(
      threadInfoProfile: ThreadInfo
    ): Boolean = ???

    override def isStepRequestWithArgsPending(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def stepRequests: Seq[StepRequestInfo] = ???

    override def removeStepRequests(
      threadInfoProfile: ThreadInfo
    ): Seq[StepRequestInfo] = ???

    override def removeStepRequestWithArgs(
      threadInfoProfile: ThreadInfo,
      extraArguments: JDIArgument*
    ): Option[StepRequestInfo] = ???

    override def removeAllStepRequests(): Seq[StepRequestInfo] = ???
  }

  describe("StepRequest") {
    describe("#stepIntoLine") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepRequest#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepRequest {
          override def stepIntoLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepOverLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepIntoMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def tryCreateStepListenerWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Try[IdentityPipeline[(StepEventInfo, Seq[JDIEventDataResult])]] = ???

          override def isStepRequestPending(
            threadInfoProfile: ThreadInfo
          ): Boolean = ???

          override def isStepRequestWithArgsPending(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Boolean = ???

          override def stepRequests: Seq[StepRequestInfo] = ???

          override def removeStepRequests(
            threadInfoProfile: ThreadInfo
          ): Seq[StepRequestInfo] = ???

          override def removeStepRequestWithArgs(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Option[StepRequestInfo] = ???

          override def removeAllStepRequests(): Seq[StepRequestInfo] = ???
        }

        val actual = stepProfile.stepIntoLine(mockThreadInfoProfile)

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be(expected)
        }
      }
    }

    describe("#stepOverLine") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepRequest#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepRequest {
          override def stepIntoLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepOutLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepIntoMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def tryCreateStepListenerWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Try[IdentityPipeline[(StepEventInfo, Seq[JDIEventDataResult])]] = ???

          override def isStepRequestPending(
            threadInfoProfile: ThreadInfo
          ): Boolean = ???

          override def isStepRequestWithArgsPending(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Boolean = ???

          override def stepRequests: Seq[StepRequestInfo] = ???

          override def removeStepRequests(
            threadInfoProfile: ThreadInfo
          ): Seq[StepRequestInfo] = ???

          override def removeStepRequestWithArgs(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Option[StepRequestInfo] = ???

          override def removeAllStepRequests(): Seq[StepRequestInfo] = ???
        }

        val actual = stepProfile.stepOverLine(mockThreadInfoProfile)

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be(expected)
        }
      }
    }

    describe("#stepOutLine") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepRequest#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepRequest {
          override def stepIntoLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepIntoMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def tryCreateStepListenerWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Try[IdentityPipeline[(StepEventInfo, Seq[JDIEventDataResult])]] = ???

          override def isStepRequestPending(
            threadInfoProfile: ThreadInfo
          ): Boolean = ???

          override def isStepRequestWithArgsPending(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Boolean = ???

          override def stepRequests: Seq[StepRequestInfo] = ???

          override def removeStepRequests(
            threadInfoProfile: ThreadInfo
          ): Seq[StepRequestInfo] = ???

          override def removeStepRequestWithArgs(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Option[StepRequestInfo] = ???

          override def removeAllStepRequests(): Seq[StepRequestInfo] = ???
        }

        val actual = stepProfile.stepOutLine(mockThreadInfoProfile)

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be (expected)
        }
      }
    }

    describe("#stepIntoMin") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepRequest#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepRequest {
          override def stepIntoLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepIntoMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepOutMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def tryCreateStepListenerWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Try[IdentityPipeline[(StepEventInfo, Seq[JDIEventDataResult])]] = ???

          override def isStepRequestPending(
            threadInfoProfile: ThreadInfo
          ): Boolean = ???

          override def isStepRequestWithArgsPending(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Boolean = ???

          override def stepRequests: Seq[StepRequestInfo] = ???

          override def removeStepRequests(
            threadInfoProfile: ThreadInfo
          ): Seq[StepRequestInfo] = ???

          override def removeStepRequestWithArgs(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Option[StepRequestInfo] = ???

          override def removeAllStepRequests(): Seq[StepRequestInfo] = ???
        }

        val actual = stepProfile.stepIntoMin(mockThreadInfoProfile)

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be(expected)
        }
      }
    }

    describe("#stepOverMin") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepRequest#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepRequest {
          override def stepIntoLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepIntoMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = {
            futureWithData
          }

          override def tryCreateStepListenerWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Try[IdentityPipeline[(StepEventInfo, Seq[JDIEventDataResult])]] = ???

          override def isStepRequestPending(
            threadInfoProfile: ThreadInfo
          ): Boolean = ???

          override def isStepRequestWithArgsPending(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Boolean = ???

          override def stepRequests: Seq[StepRequestInfo] = ???

          override def removeStepRequests(
            threadInfoProfile: ThreadInfo
          ): Seq[StepRequestInfo] = ???

          override def removeStepRequestWithArgs(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Option[StepRequestInfo] = ???

          override def removeAllStepRequests(): Seq[StepRequestInfo] = ???
        }

        val actual = stepProfile.stepOverMin(mockThreadInfoProfile)

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be(expected)
        }
      }
    }

    describe("#stepOutMin") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepRequest#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepRequest {
          override def stepIntoLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepIntoMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = {
            futureWithData
          }

          override def stepOverMinWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def tryCreateStepListenerWithData(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Try[IdentityPipeline[(StepEventInfo, Seq[JDIEventDataResult])]] = ???

          override def isStepRequestPending(
            threadInfoProfile: ThreadInfo
          ): Boolean = ???

          override def isStepRequestWithArgsPending(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Boolean = ???

          override def stepRequests: Seq[StepRequestInfo] = ???

          override def removeStepRequests(
            threadInfoProfile: ThreadInfo
          ): Seq[StepRequestInfo] = ???

          override def removeStepRequestWithArgs(
            threadInfoProfile: ThreadInfo,
            extraArguments: JDIArgument*
          ): Option[StepRequestInfo] = ???

          override def removeAllStepRequests(): Seq[StepRequestInfo] = ???
        }

        val actual = stepProfile.stepOutMin(mockThreadInfoProfile)

        // Funnel the data through the future that is mapped by the wrapper
        // method
        promiseWithData.success(data)

        whenReady(actual) { a =>
          a should be (expected)
        }
      }
    }

    describe("#tryCreateStepListener") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: StepEventInfo = null
        successStepProfile
          .tryCreateStepListener(mockThreadInfoProfile)
          .get
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        var actual: Throwable = null
        failStepProfile
          .tryCreateStepListener(mockThreadInfoProfile)
          .failed
          .foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#createStepListener") {
      it("should return a pipeline of events if successful") {
        val expected = mock[StepEventInfo]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: StepEventInfo = null
        successStepProfile
          .createStepListener(mockThreadInfoProfile)
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failStepProfile.createStepListener(mockThreadInfoProfile)
        }
      }
    }

    describe("#createStepListenerWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[StepEventInfo], Seq(mock[JDIEventDataResult]))

        var actual: (StepEventInfo, Seq[JDIEventDataResult]) = null
        successStepProfile
          .createStepListenerWithData(mockThreadInfoProfile)
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failStepProfile
            .createStepListenerWithData(mockThreadInfoProfile)
        }
      }
    }
  }
}
