package org.scaladebugger.api.dsl.vm

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.events.VMDeathEventInfoProfile
import org.scaladebugger.api.profiles.traits.vm.VMDeathProfile

import scala.util.Success

class VMDeathDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val mockVMDeathProfile = mock[VMDeathProfile]

  describe("VMDeathDSLWrapper") {
    describe("#onVMDeath") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.VMDeathDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[VMDeathEventInfoProfile]))

        (mockVMDeathProfile.tryGetOrCreateVMDeathRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockVMDeathProfile.onVMDeath(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeVMDeath") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.VMDeathDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[VMDeathEventInfoProfile])

        (mockVMDeathProfile.getOrCreateVMDeathRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockVMDeathProfile.onUnsafeVMDeath(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onVMDeathWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.VMDeathDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(VMDeathEventInfoProfile, Seq[JDIEventDataResult])]
        ))

        (mockVMDeathProfile.tryGetOrCreateVMDeathRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockVMDeathProfile.onVMDeathWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeVMDeathWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.VMDeathDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(VMDeathEventInfoProfile, Seq[JDIEventDataResult])]
        )

        (mockVMDeathProfile.getOrCreateVMDeathRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockVMDeathProfile.onUnsafeVMDeathWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
