package org.scaladebugger.api.dsl.vm

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.events.VMDisconnectEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.vm.VMDisconnectProfile

import scala.util.Success

class VMDisconnectDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockVMDisconnectProfile = mock[VMDisconnectProfile]

  describe("VMDisconnectDSLWrapper") {
    describe("#onVMDisconnect") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.VMDisconnectDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[VMDisconnectEventInfoProfile]))

        (mockVMDisconnectProfile.tryGetOrCreateVMDisconnectRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockVMDisconnectProfile.onVMDisconnect(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeVMDisconnect") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.VMDisconnectDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[VMDisconnectEventInfoProfile])

        (mockVMDisconnectProfile.getOrCreateVMDisconnectRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockVMDisconnectProfile.onUnsafeVMDisconnect(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onVMDisconnectWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.VMDisconnectDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(VMDisconnectEventInfoProfile, Seq[JDIEventDataResult])]
        ))

        (mockVMDisconnectProfile.tryGetOrCreateVMDisconnectRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockVMDisconnectProfile.onVMDisconnectWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeVMDisconnectWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.VMDisconnectDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(VMDisconnectEventInfoProfile, Seq[JDIEventDataResult])]
        )

        (mockVMDisconnectProfile.getOrCreateVMDisconnectRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockVMDisconnectProfile.onUnsafeVMDisconnectWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
