package org.scaladebugger.api.dsl.exceptions

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.exceptions.ExceptionProfile
import org.scaladebugger.api.profiles.traits.info.events.ExceptionEventInfoProfile

import scala.util.Success

class ExceptionDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockExceptionProfile = mock[ExceptionProfile]

  describe("ExceptionDSLWrapper") {
    describe("#onException") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ExceptionDSL

        val exceptionName = "SomeException"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[ExceptionEventInfoProfile]))

        (mockExceptionProfile.tryGetOrCreateExceptionRequest _).expects(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments
        ).returning(returnValue).once()

        mockExceptionProfile.onException(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeException") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ExceptionDSL

        val exceptionName = "SomeException"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[ExceptionEventInfoProfile])

        (mockExceptionProfile.getOrCreateExceptionRequest _).expects(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments
        ).returning(returnValue).once()

        mockExceptionProfile.onUnsafeException(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onExceptionWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ExceptionDSL

        val exceptionName = "SomeException"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(ExceptionEventInfoProfile, Seq[JDIEventDataResult])]
        ))

        (mockExceptionProfile.tryGetOrCreateExceptionRequestWithData _).expects(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments
        ).returning(returnValue).once()

        mockExceptionProfile.onExceptionWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeExceptionWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ExceptionDSL

        val exceptionName = "SomeException"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(ExceptionEventInfoProfile, Seq[JDIEventDataResult])]
        )

        (mockExceptionProfile.getOrCreateExceptionRequestWithData _).expects(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments
        ).returning(returnValue).once()

        mockExceptionProfile.onUnsafeExceptionWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onAllExceptions") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ExceptionDSL

        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[ExceptionEventInfoProfile]))

        (mockExceptionProfile.tryGetOrCreateAllExceptionsRequest _).expects(
          notifyCaught,
          notifyUncaught,
          extraArguments
        ).returning(returnValue).once()

        mockExceptionProfile.onAllExceptions(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeAllExceptions") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ExceptionDSL

        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[ExceptionEventInfoProfile])

        (mockExceptionProfile.getOrCreateAllExceptionsRequest _).expects(
          notifyCaught,
          notifyUncaught,
          extraArguments
        ).returning(returnValue).once()

        mockExceptionProfile.onUnsafeAllExceptions(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onAllExceptionsWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ExceptionDSL

        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(ExceptionEventInfoProfile, Seq[JDIEventDataResult])]
        ))

        (mockExceptionProfile.tryGetOrCreateAllExceptionsRequestWithData _).expects(
          notifyCaught,
          notifyUncaught,
          extraArguments
        ).returning(returnValue).once()

        mockExceptionProfile.onAllExceptionsWithData(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeAllExceptionsWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ExceptionDSL

        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(ExceptionEventInfoProfile, Seq[JDIEventDataResult])]
        )

        (mockExceptionProfile.getOrCreateAllExceptionsRequestWithData _).expects(
          notifyCaught,
          notifyUncaught,
          extraArguments
        ).returning(returnValue).once()

        mockExceptionProfile.onUnsafeAllExceptionsWithData(
          notifyCaught,
          notifyUncaught,
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
