package org.scaladebugger.api.profiles.traits.steps
import com.sun.jdi.event.StepEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.steps.StepRequestInfo
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.ThreadInfoProfile
import org.scaladebugger.api.profiles.traits.info.events.StepEventInfoProfile
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.concurrent._
import scala.util.{Failure, Success, Try}

class StepProfileSpec extends FunSpec with Matchers with ParallelTestExecution
  with MockFactory with ScalaFutures
{
  private val mockThreadInfoProfile = mock[ThreadInfoProfile]

  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[StepProfile#StepEventAndData]
  )

  private val successStepProfile = new Object with StepProfile {
    override def stepIntoLineWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Future[StepEventAndData] = ???

    override def stepOverLineWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Future[StepEventAndData] = ???

    override def stepOutLineWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Future[StepEventAndData] = ???

    override def stepIntoMinWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Future[(StepEventAndData)] = ???

    override def stepOutMinWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Future[(StepEventAndData)] = ???

    override def stepOverMinWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Future[(StepEventAndData)] = ???

    override def tryCreateStepListenerWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[StepEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def isStepRequestPending(
      threadInfoProfile: ThreadInfoProfile
    ): Boolean = ???

    override def isStepRequestWithArgsPending(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def stepRequests: Seq[StepRequestInfo] = ???

    override def removeStepRequests(
      threadInfoProfile: ThreadInfoProfile
    ): Seq[StepRequestInfo] = ???

    override def removeStepRequestWithArgs(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Option[StepRequestInfo] = ???

    override def removeAllStepRequests(): Seq[StepRequestInfo] = ???
  }

  private val failStepProfile = new Object with StepProfile {
    override def stepIntoLineWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Future[StepEventAndData] = ???

    override def stepOverLineWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Future[StepEventAndData] = ???

    override def stepOutLineWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Future[StepEventAndData] = ???

    override def stepIntoMinWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Future[(StepEventAndData)] = ???

    override def stepOutMinWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Future[(StepEventAndData)] = ???

    override def stepOverMinWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Future[(StepEventAndData)] = ???

    override def tryCreateStepListenerWithData(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[StepEventAndData]] = {
      Failure(TestThrowable)
    }

    override def isStepRequestPending(
      threadInfoProfile: ThreadInfoProfile
    ): Boolean = ???

    override def isStepRequestWithArgsPending(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Boolean = ???

    override def stepRequests: Seq[StepRequestInfo] = ???

    override def removeStepRequests(
      threadInfoProfile: ThreadInfoProfile
    ): Seq[StepRequestInfo] = ???

    override def removeStepRequestWithArgs(
      threadInfoProfile: ThreadInfoProfile,
      extraArguments: JDIArgument*
    ): Option[StepRequestInfo] = ???

    override def removeAllStepRequests(): Seq[StepRequestInfo] = ???
  }

  describe("StepProfile") {
    describe("#stepIntoLine") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[StepEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepIntoLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepOverLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepIntoMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def tryCreateStepListenerWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Try[IdentityPipeline[(StepEventInfoProfile, Seq[JDIEventDataResult])]] = ???

          override def isStepRequestPending(
            threadInfoProfile: ThreadInfoProfile
          ): Boolean = ???

          override def isStepRequestWithArgsPending(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Boolean = ???

          override def stepRequests: Seq[StepRequestInfo] = ???

          override def removeStepRequests(
            threadInfoProfile: ThreadInfoProfile
          ): Seq[StepRequestInfo] = ???

          override def removeStepRequestWithArgs(
            threadInfoProfile: ThreadInfoProfile,
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
        val expected = mock[StepEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepIntoLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepOutLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepIntoMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def tryCreateStepListenerWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Try[IdentityPipeline[(StepEventInfoProfile, Seq[JDIEventDataResult])]] = ???

          override def isStepRequestPending(
            threadInfoProfile: ThreadInfoProfile
          ): Boolean = ???

          override def isStepRequestWithArgsPending(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Boolean = ???

          override def stepRequests: Seq[StepRequestInfo] = ???

          override def removeStepRequests(
            threadInfoProfile: ThreadInfoProfile
          ): Seq[StepRequestInfo] = ???

          override def removeStepRequestWithArgs(
            threadInfoProfile: ThreadInfoProfile,
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
        val expected = mock[StepEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepIntoLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepIntoMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def tryCreateStepListenerWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Try[IdentityPipeline[(StepEventInfoProfile, Seq[JDIEventDataResult])]] = ???

          override def isStepRequestPending(
            threadInfoProfile: ThreadInfoProfile
          ): Boolean = ???

          override def isStepRequestWithArgsPending(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Boolean = ???

          override def stepRequests: Seq[StepRequestInfo] = ???

          override def removeStepRequests(
            threadInfoProfile: ThreadInfoProfile
          ): Seq[StepRequestInfo] = ???

          override def removeStepRequestWithArgs(
            threadInfoProfile: ThreadInfoProfile,
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
        val expected = mock[StepEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepIntoLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepIntoMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = {
            futureWithData
          }

          override def stepOutMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def tryCreateStepListenerWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Try[IdentityPipeline[(StepEventInfoProfile, Seq[JDIEventDataResult])]] = ???

          override def isStepRequestPending(
            threadInfoProfile: ThreadInfoProfile
          ): Boolean = ???

          override def isStepRequestWithArgsPending(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Boolean = ???

          override def stepRequests: Seq[StepRequestInfo] = ???

          override def removeStepRequests(
            threadInfoProfile: ThreadInfoProfile
          ): Seq[StepRequestInfo] = ???

          override def removeStepRequestWithArgs(
            threadInfoProfile: ThreadInfoProfile,
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
        val expected = mock[StepEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepIntoLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepIntoMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOverMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = {
            futureWithData
          }

          override def tryCreateStepListenerWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Try[IdentityPipeline[(StepEventInfoProfile, Seq[JDIEventDataResult])]] = ???

          override def isStepRequestPending(
            threadInfoProfile: ThreadInfoProfile
          ): Boolean = ???

          override def isStepRequestWithArgsPending(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Boolean = ???

          override def stepRequests: Seq[StepRequestInfo] = ???

          override def removeStepRequests(
            threadInfoProfile: ThreadInfoProfile
          ): Seq[StepRequestInfo] = ???

          override def removeStepRequestWithArgs(
            threadInfoProfile: ThreadInfoProfile,
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
        val expected = mock[StepEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val promiseWithData = Promise[StepProfile#StepEventAndData]()
        val futureWithData = promiseWithData.future

        val stepProfile = new Object with StepProfile {
          override def stepIntoLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOverLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepOutLineWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[StepEventAndData] = ???

          override def stepIntoMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def stepOutMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = {
            futureWithData
          }

          override def stepOverMinWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Future[(StepEventAndData)] = ???

          override def tryCreateStepListenerWithData(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Try[IdentityPipeline[(StepEventInfoProfile, Seq[JDIEventDataResult])]] = ???

          override def isStepRequestPending(
            threadInfoProfile: ThreadInfoProfile
          ): Boolean = ???

          override def isStepRequestWithArgsPending(
            threadInfoProfile: ThreadInfoProfile,
            extraArguments: JDIArgument*
          ): Boolean = ???

          override def stepRequests: Seq[StepRequestInfo] = ???

          override def removeStepRequests(
            threadInfoProfile: ThreadInfoProfile
          ): Seq[StepRequestInfo] = ???

          override def removeStepRequestWithArgs(
            threadInfoProfile: ThreadInfoProfile,
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
        val expected = mock[StepEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: StepEventInfoProfile = null
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
        val expected = mock[StepEventInfoProfile]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: StepEventInfoProfile = null
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
        val expected = (mock[StepEventInfoProfile], Seq(mock[JDIEventDataResult]))

        var actual: (StepEventInfoProfile, Seq[JDIEventDataResult]) = null
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
