package org.scaladebugger.api.dsl.vm

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.events.VMStartEventInfo
import org.scaladebugger.api.profiles.traits.requests.vm.VMStartRequest
import org.scaladebugger.test.helpers.ParallelMockFunSpec

import scala.util.Success

class VMStartDSLWrapperSpec extends ParallelMockFunSpec
{
  private val mockVMStartProfile = mock[VMStartRequest]

  describe("VMStartDSLWrapper") {
    describe("#onVMStart") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.VMStartDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[VMStartEventInfo]))

        (mockVMStartProfile.tryGetOrCreateVMStartRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockVMStartProfile.onVMStart(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeVMStart") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.VMStartDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[VMStartEventInfo])

        (mockVMStartProfile.getOrCreateVMStartRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockVMStartProfile.onUnsafeVMStart(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onVMStartWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.VMStartDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(VMStartEventInfo, Seq[JDIEventDataResult])]
        ))

        (mockVMStartProfile.tryGetOrCreateVMStartRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockVMStartProfile.onVMStartWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeVMStartWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.VMStartDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(VMStartEventInfo, Seq[JDIEventDataResult])]
        )

        (mockVMStartProfile.getOrCreateVMStartRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockVMStartProfile.onUnsafeVMStartWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
