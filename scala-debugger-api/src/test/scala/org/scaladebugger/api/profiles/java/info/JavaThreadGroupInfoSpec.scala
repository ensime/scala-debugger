package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class JavaThreadGroupInfoSpec extends ParallelMockFunSpec
{
  private val mockNewThreadGroupProfile = mockFunction[ThreadGroupReference, ThreadGroupInfo]
  private val mockNewThreadProfile = mockFunction[ThreadReference, ThreadInfo]

  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockReferenceType = mock[ReferenceType]
  private val mockThreadGroupReference = mock[ThreadGroupReference]
  private val javaThreadGroupInfoProfile = new JavaThreadGroupInfo(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockThreadGroupReference
  )(
    _virtualMachine = mockVirtualMachine,
    _referenceType = mockReferenceType
  ) {
    override protected def newThreadGroupProfile(
      threadGroupReference: ThreadGroupReference
    ): ThreadGroupInfo = mockNewThreadGroupProfile(threadGroupReference)

    override protected def newThreadProfile(
      threadReference: ThreadReference
    ): ThreadInfo = mockNewThreadProfile(threadReference)
  }

  describe("JavaThreadGroupInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ThreadGroupInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducerProfile.newThreadGroupInfo(
          _: ScalaVirtualMachine,
          _: ThreadGroupReference
        )(
          _: VirtualMachine,
          _: ReferenceType
        )).expects(
          mockScalaVirtualMachine,
          mockThreadGroupReference,
          *, *
        ).returning(expected).once()

        val actual = javaThreadGroupInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaThreadGroupInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockThreadGroupReference

        val actual = javaThreadGroupInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the name of the thread group") {
        val expected = "some name"

        (mockThreadGroupReference.name _).expects().returning(expected).once()

        val actual = javaThreadGroupInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#parent") {
      it("should return Some(thread group) if a parent exists") {
        val expected = Some(mock[ThreadGroupInfo])

        val parentThreadGroup = mock[ThreadGroupReference]
        (mockThreadGroupReference.parent _).expects()
          .returning(parentThreadGroup).once()

        mockNewThreadGroupProfile.expects(parentThreadGroup)
          .returning(expected.get).once()

        val actual = javaThreadGroupInfoProfile.parent

        actual should be (expected)
      }

      it("should return None if no parent exists") {
        val expected = None

        (mockThreadGroupReference.parent _).expects().returning(null).once()

        val actual = javaThreadGroupInfoProfile.parent

        actual should be (expected)
      }
    }

    describe("#suspend") {
      it("should invoke suspend on the underlying thread group") {
        (mockThreadGroupReference.suspend _).expects().once()

        javaThreadGroupInfoProfile.suspend()
      }
    }

    describe("#resume") {
      it("should invoke resume on the underlying thread group") {
        (mockThreadGroupReference.resume _).expects().once()

        javaThreadGroupInfoProfile.resume()
      }
    }

    describe("#threadGroups") {
      it("should return a collection of profiles wrapping sub thread groups") {
        val expected = Seq(mock[ThreadGroupInfo])

        import scala.collection.JavaConverters._
        val mockThreadGroups = expected.map(_ => mock[ThreadGroupReference])
        (mockThreadGroupReference.threadGroups _).expects()
          .returning(mockThreadGroups.asJava).once()

        expected.zip(mockThreadGroups).foreach { case (e, tg) =>
          mockNewThreadGroupProfile.expects(tg).returning(e).once()
        }

        val actual = javaThreadGroupInfoProfile.threadGroups

        actual should be (expected)
      }
    }

    describe("#threads") {
      it("should return a collection of profiles wrapping threads in the group") {
        val expected = Seq(mock[ThreadInfo])

        import scala.collection.JavaConverters._
        val mockThreads = expected.map(_ => mock[ThreadReference])
        (mockThreadGroupReference.threads _).expects()
          .returning(mockThreads.asJava).once()

        expected.zip(mockThreads).foreach { case (e, t) =>
          mockNewThreadProfile.expects(t).returning(e).once()
        }

        val actual = javaThreadGroupInfoProfile.threads

        actual should be (expected)
      }
    }
  }
}
