package org.scaladebugger.api.profiles.swappable.info
import acyclic.file
import com.sun.jdi.{ObjectReference, ThreadReference}
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.api.profiles.traits.info.{ObjectInfoProfile, ReferenceTypeInfoProfile, ThreadInfoProfile, ValueInfoProfile}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class SwappableGrabInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableGrabInfoProfile") {
    describe("#`object`") {
      it("should invoke the method on the underlying profile") {
        val expected = mock[ObjectInfoProfile]
        val mockThreadReference = mock[ThreadReference]
        val mockObjectReference = mock[ObjectReference]

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.`object`(_: ThreadReference, _: ObjectReference))
          .expects(mockThreadReference, mockObjectReference)
          .returning(expected).once()

        val actual = swappableDebugProfile.`object`(
          mockThreadReference,
          mockObjectReference
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val mockThreadReference = mock[ThreadReference]
        val mockObjectReference = mock[ObjectReference]

        intercept[AssertionError] {
          (mockProfileManager.retrieve _).expects(*).returning(None).once()
          swappableDebugProfile.`object`(
            mockThreadReference,
            mockObjectReference
          )
        }
      }
    }

    describe("#thread") {
      it("should invoke the method on the underlying profile") {
        val expected = mock[ThreadInfoProfile]

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).twice()

        (mockDebugProfile.thread(_: Long)).expects(*)
          .returning(expected).once()

        (mockDebugProfile.thread(_: ThreadReference)).expects(*)
          .returning(expected).once()

        swappableDebugProfile.thread(0L) should be(expected)
        swappableDebugProfile.thread(mock[ThreadReference]) should be(expected)
      }

      it("should throw an exception if there is no underlying profile") {
        intercept[AssertionError] {
          (mockProfileManager.retrieve _).expects(*).returning(None).once()
          swappableDebugProfile.thread(0L)
        }

        intercept[AssertionError] {
          (mockProfileManager.retrieve _).expects(*).returning(None).once()
          swappableDebugProfile.thread(mock[ThreadReference])
        }
      }
    }

    describe("#classes") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(mock[ReferenceTypeInfoProfile])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.classes _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.classes

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        intercept[AssertionError] {
          (mockProfileManager.retrieve _).expects(*).returning(None).once()
          swappableDebugProfile.classes
        }
      }
    }
  }
}
