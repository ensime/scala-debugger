package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureThreadInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockReferenceType = mock[ReferenceType]
  private val mockNewFrameProfile = mockFunction[StackFrame, Int, FrameInfoProfile]
  private val mockNewThreadStatusProfile = mockFunction[ThreadStatusInfoProfile]
  private val mockNewThreadGroupProfile = mockFunction[ThreadGroupReference, ThreadGroupInfoProfile]
  private val mockThreadReference = mock[ThreadReference]
  private val pureThreadInfoProfile = new PureThreadInfoProfile(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockThreadReference
  )(
    _virtualMachine = mockVirtualMachine,
    _referenceType = mockReferenceType
  ) {

    override protected def newThreadStatusProfile(): ThreadStatusInfoProfile =
      mockNewThreadStatusProfile()

    override protected def newThreadGroupProfile(
      threadGroupReference: ThreadGroupReference
    ): ThreadGroupInfoProfile = mockNewThreadGroupProfile(threadGroupReference)

    override protected def newFrameProfile(
      stackFrame: StackFrame,
      index: Int
    ): FrameInfoProfile = mockNewFrameProfile(stackFrame, index)
  }

  describe("PureThreadInfoProfile") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ThreadInfoProfile]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducerProfile.newThreadInfoProfile(
          _: ScalaVirtualMachine,
          _: ThreadReference
        )(
          _: VirtualMachine,
          _: ReferenceType
        )).expects(
          mockScalaVirtualMachine,
          mockThreadReference,
          *, *
        ).returning(expected).once()

        val actual = pureThreadInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureThreadInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockThreadReference

        val actual = pureThreadInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the name of the thread") {
        val expected = "some name"

        (mockThreadReference.name _).expects().returning(expected).once()

        val actual = pureThreadInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#status") {
      it("should return a new thread status info profile") {
        val expected = mock[ThreadStatusInfoProfile]

        mockNewThreadStatusProfile.expects().returning(expected).once()
        val actual = pureThreadInfoProfile.status

        actual should be (expected)
      }
    }

    describe("#threadGroup") {
      it("should return a new thread group info profile wrapping the thread group") {
        val expected = mock[ThreadGroupInfoProfile]

        val mockThreadGroupReference = mock[ThreadGroupReference]

        (mockThreadReference.threadGroup _).expects()
          .returning(mockThreadGroupReference).once()

        mockNewThreadGroupProfile.expects(mockThreadGroupReference)
          .returning(expected).once()

        val actual = pureThreadInfoProfile.threadGroup

        actual should be (expected)
      }
    }

    describe("#suspend") {
      it("should suspend the underlying thread") {
        (mockThreadReference.suspend _).expects().once()

        pureThreadInfoProfile.suspend()
      }
    }

    describe("#resume") {
      it("should resume the underlying thread") {
        (mockThreadReference.resume _).expects().once()

        pureThreadInfoProfile.resume()
      }
    }

    describe("#frames()") {
      it("should return a collection of profiles wrapping the frames of the suspended thread") {
        val expected = Seq(mock[FrameInfoProfile])

        val mockStackFrames = Seq(mock[StackFrame])

        // Retrieve raw stack frame
        import scala.collection.JavaConverters._
        (mockThreadReference.frames: Function0[java.util.List[StackFrame]])
          .expects()
          .returning(mockStackFrames.asJava)
          .once()

        // Wrap stack frame in profile instance
        mockStackFrames.zip(expected).zipWithIndex.foreach { case ((sf, e), i) =>
          mockNewFrameProfile.expects(sf, i).returning(e).once()
        }

        val actual = pureThreadInfoProfile.frames

        actual should be (expected)
      }
    }

    describe("#frames(index, length)") {
      it("should return a collection of profiles wrapping the frames of the suspended thread") {
        val index = 1
        val length = 2
        val totalFrames = 4

        val expected = (1 to totalFrames).map(_ => mock[FrameInfoProfile])
        val mockStackFrames = (1 to totalFrames).map(_ => mock[StackFrame])

        // Retrieve raw stack frame
        import scala.collection.JavaConverters._

        (mockThreadReference.frameCount _).expects()
          .returning(totalFrames).once()

        (mockThreadReference.frames(_: Int, _: Int))
          .expects(index, length)
          .returning(mockStackFrames.asJava)
          .once()

        // Wrap stack frame in profile instance (with index used as offset)
        mockStackFrames.zip(expected).zipWithIndex.foreach { case ((sf, e), i) =>
          mockNewFrameProfile.expects(sf, i + index).returning(e).once()
        }

        val actual = pureThreadInfoProfile.frames(index, length)

        actual should be (expected)
      }
    }

    describe("#frame") {
      it("should return a profile wrapping the frame at the specified position in the stack") {
        val expected = mock[FrameInfoProfile]

        val index = 999
        val mockStackFrame = mock[StackFrame]

        // Retrieve raw stack frame
        (mockThreadReference.frame _).expects(index)
          .returning(mockStackFrame).once()

        // Wrap stack frame in profile instance
        mockNewFrameProfile.expects(mockStackFrame, index)
          .returning(expected).once()

        val actual = pureThreadInfoProfile.frame(index)

        actual should be (expected)
      }
    }

    describe("#totalFrames") {
      it("should return the total number of frames available in the stack of the suspended thread") {
        val expected = 999

        (mockThreadReference.frameCount _).expects().returning(expected).once()

        val actual = pureThreadInfoProfile.totalFrames

        actual should be (expected)
      }
    }
  }
}
