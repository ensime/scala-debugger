package org.scaladebugger.api.dsl.watchpoints

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.events.ModificationWatchpointEventInfo
import org.scaladebugger.api.profiles.traits.requests.watchpoints.ModificationWatchpointRequest
import org.scaladebugger.test.helpers.ParallelMockFunSpec

import scala.util.Success

class ModificationWatchpointDSLWrapperSpec extends ParallelMockFunSpec
{
  private val mockModificationWatchpointProfile = mock[ModificationWatchpointRequest]

  describe("ModificationWatchpointDSLWrapper") {
    describe("#onModificationWatchpoint") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.ModificationWatchpointDSL

        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[ModificationWatchpointEventInfo]))

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
        val returnValue = Pipeline.newPipeline(classOf[ModificationWatchpointEventInfo])

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
          classOf[(ModificationWatchpointEventInfo, Seq[JDIEventDataResult])]
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
          classOf[(ModificationWatchpointEventInfo, Seq[JDIEventDataResult])]
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
