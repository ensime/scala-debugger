package org.scaladebugger.api.dsl.watchpoints

import com.sun.jdi.event.ModificationWatchpointEvent
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.watchpoints.ModificationWatchpointProfile
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.Success

class ModificationWatchpointDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockModificationWatchpointProfile = mock[ModificationWatchpointProfile]

  describe("ModificationWatchpointDSLWrapper") {
    describe("#onModificationWatchpoint") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ModificationWatchpointDSL

        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[ModificationWatchpointEvent]))

        (mockModificationWatchpointProfile.tryGetOrCreateModificationWatchpointRequest _).expects(
          className,
          fieldName,
          extraArguments
        ).returning(returnValue).once()

        mockModificationWatchpointProfile.onModificationWatchpoint(
          className,
          fieldName,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeModificationWatchpoint") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ModificationWatchpointDSL

        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[ModificationWatchpointEvent])

        (mockModificationWatchpointProfile.getOrCreateModificationWatchpointRequest _).expects(
          className,
          fieldName,
          extraArguments
        ).returning(returnValue).once()

        mockModificationWatchpointProfile.onUnsafeModificationWatchpoint(
          className,
          fieldName,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onModificationWatchpointWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ModificationWatchpointDSL

        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(ModificationWatchpointEvent, Seq[JDIEventDataResult])]
        ))

        (mockModificationWatchpointProfile.tryGetOrCreateModificationWatchpointRequestWithData _).expects(
          className,
          fieldName,
          extraArguments
        ).returning(returnValue).once()

        mockModificationWatchpointProfile.onModificationWatchpointWithData(
          className,
          fieldName,
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeModificationWatchpointWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ModificationWatchpointDSL

        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(ModificationWatchpointEvent, Seq[JDIEventDataResult])]
        )

        (mockModificationWatchpointProfile.getOrCreateModificationWatchpointRequestWithData _).expects(
          className,
          fieldName,
          extraArguments
        ).returning(returnValue).once()

        mockModificationWatchpointProfile.onUnsafeModificationWatchpointWithData(
          className,
          fieldName,
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
