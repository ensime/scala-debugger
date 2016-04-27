package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestFrameInfoProfile

import scala.util.{Failure, Success, Try}

class FrameInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("FrameInfoProfile") {
    describe("#toPrettyString") {
      it("should include the frame's location if available") {
        val expected = "Frame 1 at (LOCATION)"
        val mockUnsafeMethod = mockFunction[Try[LocationInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def index: Int = 1
          override def tryLocation: Try[LocationInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Success(mock[LocationInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        (r.get.toPrettyString _).expects().returning("LOCATION").once()

        val actual = frameInfoProfile.toPrettyString

        actual should be (expected)
      }

      it("should use ??? if the frame's location is unavailable") {
        val expected = "Frame 1 at (???)"
        val mockUnsafeMethod = mockFunction[Try[LocationInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def index: Int = 1
          override def tryLocation: Try[LocationInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Failure(new Throwable)
        mockUnsafeMethod.expects().returning(r).once()

        val actual = frameInfoProfile.toPrettyString

        actual should be (expected)
      }
    }

    describe("#hasIndex") {
      it("should return true if index >= 0") {
        val expected = true

        val frameInfoProfile = new TestFrameInfoProfile {
          override def index: Int = 0
        }

        val actual = frameInfoProfile.hasIndex

        actual should be (expected)
      }

      it("should return false if index < 0") {
        val expected = false

        val frameInfoProfile = new TestFrameInfoProfile {
          override def index: Int = -1
        }

        val actual = frameInfoProfile.hasIndex

        actual should be (expected)
      }
    }

    describe("#tryThisObject") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ObjectInfoProfile]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def thisObject: ObjectInfoProfile = mockUnsafeMethod()
        }

        val r = mock[ObjectInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryThisObject.get should be (r)
      }
    }

    describe("#tryCurrentThread") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadInfoProfile]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def currentThread: ThreadInfoProfile =
            mockUnsafeMethod()
        }

        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryCurrentThread.get should be (r)
      }
    }

    describe("#tryLocation") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[LocationInfoProfile]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def location: LocationInfoProfile =
            mockUnsafeMethod()
        }

        val r = mock[LocationInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryLocation.get should be (r)
      }
    }

    describe("#tryVariable") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, VariableInfoProfile]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def variable(name: String): VariableInfoProfile =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[VariableInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        frameInfoProfile.tryVariable(a1).get should be (r)
      }
    }

    describe("#tryFieldVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[VariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def fieldVariables: Seq[VariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[VariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryFieldVariables.get should be (r)
      }
    }

    describe("#tryLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[IndexedVariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def localVariables: Seq[IndexedVariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[IndexedVariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryLocalVariables.get should be (r)
      }
    }

    describe("#tryAllVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[VariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def allVariables: Seq[VariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[VariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryAllVariables.get should be (r)
      }
    }

    describe("#tryArgumentValues") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ValueInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def argumentValues: Seq[ValueInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[ValueInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryArgumentValues.get should be (r)
      }
    }

    describe("#tryArgumentLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[IndexedVariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def argumentLocalVariables: Seq[IndexedVariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[IndexedVariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryArgumentLocalVariables.get should be (r)
      }
    }

    describe("#tryNonArgumentLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[IndexedVariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def nonArgumentLocalVariables: Seq[IndexedVariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[IndexedVariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryNonArgumentLocalVariables.get should be (r)
      }
    }

    describe("#tryIndexedVariable") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, VariableInfoProfile]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def indexedVariable(name: String): VariableInfoProfile =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[VariableInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        frameInfoProfile.tryIndexedVariable(a1).get should be (r)
      }
    }

    describe("#tryIndexedFieldVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[VariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def indexedFieldVariables: Seq[VariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[VariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryIndexedFieldVariables.get should be (r)
      }
    }

    describe("#tryIndexedLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[IndexedVariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def indexedLocalVariables: Seq[IndexedVariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[IndexedVariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryIndexedLocalVariables.get should be (r)
      }
    }

    describe("#tryIndexedAllVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[VariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def indexedAllVariables: Seq[VariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[VariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryIndexedAllVariables.get should be (r)
      }
    }

    describe("#tryIndexedArgumentLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[IndexedVariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def indexedArgumentLocalVariables: Seq[IndexedVariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[IndexedVariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryIndexedArgumentLocalVariables.get should be (r)
      }
    }

    describe("#tryIndexedNonArgumentLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[IndexedVariableInfoProfile]]

        val frameInfoProfile = new TestFrameInfoProfile {
          override def indexedNonArgumentLocalVariables: Seq[IndexedVariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[IndexedVariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryIndexedNonArgumentLocalVariables.get should be (r)
      }
    }
  }
}
