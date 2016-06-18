package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureThreadGroupInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockNewThreadGroupProfile = mockFunction[ThreadGroupReference, ThreadGroupInfoProfile]
  private val mockNewThreadProfile = mockFunction[ThreadReference, ThreadInfoProfile]

  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockReferenceType = mock[ReferenceType]
  private val mockThreadReference = mock[ThreadReference]
  private val mockThreadGroupReference = mock[ThreadGroupReference]
  private val pureThreadGroupInfoProfile = new PureThreadGroupInfoProfile(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockThreadGroupReference
  )(
    _virtualMachine = mockVirtualMachine,
    _threadReference = mockThreadReference,
    _referenceType = mockReferenceType
  ) {
    override protected def newThreadGroupProfile(
      threadGroupReference: ThreadGroupReference
    ): ThreadGroupInfoProfile = mockNewThreadGroupProfile(threadGroupReference)

    override protected def newThreadProfile(
      threadReference: ThreadReference
    ): ThreadInfoProfile = mockNewThreadProfile(threadReference)
  }

  describe("PureThreadGroupInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockThreadGroupReference

        val actual = pureThreadGroupInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the name of the thread group") {
        val expected = "some name"

        (mockThreadGroupReference.name _).expects().returning(expected).once()

        val actual = pureThreadGroupInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#parent") {
      it("should return Some(thread group) if a parent exists") {
        val expected = Some(mock[ThreadGroupInfoProfile])

        val parentThreadGroup = mock[ThreadGroupReference]
        (mockThreadGroupReference.parent _).expects()
          .returning(parentThreadGroup).once()

        mockNewThreadGroupProfile.expects(parentThreadGroup)
          .returning(expected.get).once()

        val actual = pureThreadGroupInfoProfile.parent

        actual should be (expected)
      }

      it("should return None if no parent exists") {
        val expected = None

        (mockThreadGroupReference.parent _).expects().returning(null).once()

        val actual = pureThreadGroupInfoProfile.parent

        actual should be (expected)
      }
    }

    describe("#suspend") {
      it("should invoke suspend on the underlying thread group") {
        (mockThreadGroupReference.suspend _).expects().once()

        pureThreadGroupInfoProfile.suspend()
      }
    }

    describe("#resume") {
      it("should invoke resume on the underlying thread group") {
        (mockThreadGroupReference.resume _).expects().once()

        pureThreadGroupInfoProfile.resume()
      }
    }

    describe("#threadGroups") {
      it("should return a collection of profiles wrapping sub thread groups") {
        val expected = Seq(mock[ThreadGroupInfoProfile])

        import scala.collection.JavaConverters._
        val mockThreadGroups = expected.map(_ => mock[ThreadGroupReference])
        (mockThreadGroupReference.threadGroups _).expects()
          .returning(mockThreadGroups.asJava).once()

        expected.zip(mockThreadGroups).foreach { case (e, tg) =>
          mockNewThreadGroupProfile.expects(tg).returning(e).once()
        }

        val actual = pureThreadGroupInfoProfile.threadGroups

        actual should be (expected)
      }
    }

    describe("#threads") {
      it("should return a collection of profiles wrapping threads in the group") {
        val expected = Seq(mock[ThreadInfoProfile])

        import scala.collection.JavaConverters._
        val mockThreads = expected.map(_ => mock[ThreadReference])
        (mockThreadGroupReference.threads _).expects()
          .returning(mockThreads.asJava).once()

        expected.zip(mockThreads).foreach { case (e, t) =>
          mockNewThreadProfile.expects(t).returning(e).once()
        }

        val actual = pureThreadGroupInfoProfile.threads

        actual should be (expected)
      }
    }
  }
}
