package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestFrameInfo

import scala.util.{Failure, Success, Try}

class FrameInfoSpec extends test.ParallelMockFunSpec
{
  describe("FrameInfo") {
    describe("#toPrettyString") {
      it("should include the frame's location if available") {
        val expected = "Frame 1 at (LOCATION)"
        val mockUnsafeMethod = mockFunction[Try[LocationInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def index: Int = 1
          override def tryLocation: Try[LocationInfo] =
            mockUnsafeMethod()
        }

        val r = Success(mock[LocationInfo])
        mockUnsafeMethod.expects().returning(r).once()
        (r.get.toPrettyString _).expects().returning("LOCATION").once()

        val actual = frameInfoProfile.toPrettyString

        actual should be (expected)
      }

      it("should use ??? if the frame's location is unavailable") {
        val expected = "Frame 1 at (???)"
        val mockUnsafeMethod = mockFunction[Try[LocationInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def index: Int = 1
          override def tryLocation: Try[LocationInfo] =
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

        val frameInfoProfile = new TestFrameInfo {
          override def index: Int = 0
        }

        val actual = frameInfoProfile.hasIndex

        actual should be (expected)
      }

      it("should return false if index < 0") {
        val expected = false

        val frameInfoProfile = new TestFrameInfo {
          override def index: Int = -1
        }

        val actual = frameInfoProfile.hasIndex

        actual should be (expected)
      }
    }

    describe("#tryThisObject") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ObjectInfo]

        val frameInfoProfile = new TestFrameInfo {
          override def thisObject: ObjectInfo = mockUnsafeMethod()
        }

        val r = mock[ObjectInfo]
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryThisObject.get should be (r)
      }
    }

    describe("#thisObject") {
      it("should retrieve the value from thisObjectOption") {
        val expected = mock[ObjectInfo]
        val mockOptionMethod = mockFunction[Option[ObjectInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def thisObjectOption: Option[ObjectInfo] =
            mockOptionMethod()
        }

        mockOptionMethod.expects().returning(Some(expected)).once()

        val actual = frameInfoProfile.thisObject

        actual should be (expected)
      }

      it("should throw an exception if thisObjectOption is None") {
        val mockOptionMethod = mockFunction[Option[ObjectInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def thisObjectOption: Option[ObjectInfo] =
            mockOptionMethod()
        }

        mockOptionMethod.expects().returning(None).once()

        intercept[NoSuchElementException] {
          frameInfoProfile.thisObject
        }
      }
    }

    describe("#tryCurrentThread") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadInfo]

        val frameInfoProfile = new TestFrameInfo {
          override def currentThread: ThreadInfo =
            mockUnsafeMethod()
        }

        val r = mock[ThreadInfo]
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryCurrentThread.get should be (r)
      }
    }

    describe("#tryLocation") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[LocationInfo]

        val frameInfoProfile = new TestFrameInfo {
          override def location: LocationInfo =
            mockUnsafeMethod()
        }

        val r = mock[LocationInfo]
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryLocation.get should be (r)
      }
    }

    describe("#tryVariable") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, VariableInfo]

        val frameInfoProfile = new TestFrameInfo {
          override def variable(name: String): VariableInfo =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[VariableInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        frameInfoProfile.tryVariable(a1).get should be (r)
      }
    }

    describe("#variable") {
      it("should retrieve the value from variableOption") {
        val expected = mock[VariableInfo]
        val mockOptionMethod = mockFunction[String, Option[VariableInfo]]
        val name = "some name"

        val frameInfoProfile = new TestFrameInfo {
          override def variableOption(name: String): Option[VariableInfo] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(Some(expected)).once()

        val actual = frameInfoProfile.variable(name)

        actual should be (expected)
      }

      it("should throw an exception if variableOption is None") {
        val mockOptionMethod = mockFunction[String, Option[VariableInfo]]
        val name = "some name"

        val frameInfoProfile = new TestFrameInfo {
          override def variableOption(name: String): Option[VariableInfo] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(None).once()

        intercept[NoSuchElementException] {
          frameInfoProfile.variable(name)
        }
      }
    }

    describe("#tryFieldVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FieldVariableInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def fieldVariables: Seq[FieldVariableInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[FieldVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryFieldVariables.get should be (r)
      }
    }

    describe("#tryLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[IndexedVariableInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def localVariables: Seq[IndexedVariableInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[IndexedVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryLocalVariables.get should be (r)
      }
    }

    describe("#tryAllVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[VariableInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def allVariables: Seq[VariableInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[VariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryAllVariables.get should be (r)
      }
    }

    describe("#tryArgumentValues") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ValueInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def argumentValues: Seq[ValueInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[ValueInfo])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryArgumentValues.get should be (r)
      }
    }

    describe("#tryArgumentLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[IndexedVariableInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def argumentLocalVariables: Seq[IndexedVariableInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[IndexedVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryArgumentLocalVariables.get should be (r)
      }
    }

    describe("#tryNonArgumentLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[IndexedVariableInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def nonArgumentLocalVariables: Seq[IndexedVariableInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[IndexedVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryNonArgumentLocalVariables.get should be (r)
      }
    }

    describe("#tryIndexedVariable") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, VariableInfo]

        val frameInfoProfile = new TestFrameInfo {
          override def indexedVariable(name: String): VariableInfo =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[VariableInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        frameInfoProfile.tryIndexedVariable(a1).get should be (r)
      }
    }

    describe("#indexedVariable") {
      it("should retrieve the value from indexedVariableOption") {
        val expected = mock[VariableInfo]
        val mockOptionMethod = mockFunction[String, Option[VariableInfo]]
        val name = "some name"

        val frameInfoProfile = new TestFrameInfo {
          override def indexedVariableOption(name: String): Option[VariableInfo] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(Some(expected)).once()

        val actual = frameInfoProfile.indexedVariable(name)

        actual should be (expected)
      }

      it("should throw an exception if indexedVariableOption is None") {
        val mockOptionMethod = mockFunction[String, Option[VariableInfo]]
        val name = "some name"

        val frameInfoProfile = new TestFrameInfo {
          override def indexedVariableOption(name: String): Option[VariableInfo] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(None).once()

        intercept[NoSuchElementException] {
          frameInfoProfile.indexedVariable(name)
        }
      }
    }

    describe("#tryIndexedFieldVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FieldVariableInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def indexedFieldVariables: Seq[FieldVariableInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[FieldVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryIndexedFieldVariables.get should be (r)
      }
    }

    describe("#tryIndexedLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[IndexedVariableInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def indexedLocalVariables: Seq[IndexedVariableInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[IndexedVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryIndexedLocalVariables.get should be (r)
      }
    }

    describe("#tryIndexedAllVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[VariableInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def indexedAllVariables: Seq[VariableInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[VariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryIndexedAllVariables.get should be (r)
      }
    }

    describe("#tryIndexedArgumentLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[IndexedVariableInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def indexedArgumentLocalVariables: Seq[IndexedVariableInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[IndexedVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryIndexedArgumentLocalVariables.get should be (r)
      }
    }

    describe("#tryIndexedNonArgumentLocalVariables") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[IndexedVariableInfo]]

        val frameInfoProfile = new TestFrameInfo {
          override def indexedNonArgumentLocalVariables: Seq[IndexedVariableInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[IndexedVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        frameInfoProfile.tryIndexedNonArgumentLocalVariables.get should be (r)
      }
    }
  }
}
