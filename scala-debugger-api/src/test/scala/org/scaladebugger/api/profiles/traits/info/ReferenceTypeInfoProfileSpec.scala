package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestReferenceTypeInfoProfile

import scala.util.{Success, Try}

class ReferenceTypeInfoProfileSpec extends test.ParallelMockFunSpec
{
  describe("ReferenceTypeInfoProfile") {
    describe("#toPrettyString") {
      it("should display the reference type name") {
        val expected = "some.class.name"

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def name: String = "some.class.name"
        }

        val actual = referenceTypeInfoProfile.toPrettyString

        actual should be(expected)
      }
    }

    describe("#classLoader") {
      it("should retrieve the value from classLoaderOption") {
        val expected = mock[ClassLoaderInfoProfile]
        val mockOptionMethod = mockFunction[Option[ClassLoaderInfoProfile]]

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def classLoaderOption: Option[ClassLoaderInfoProfile] =
            mockOptionMethod()
        }

        mockOptionMethod.expects().returning(Some(expected)).once()

        val actual = referenceTypeInfoProfile.classLoader

        actual should be (expected)
      }

      it("should throw an exception if classLoaderOption is None") {
        val mockOptionMethod = mockFunction[Option[ClassLoaderInfoProfile]]

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def classLoaderOption: Option[ClassLoaderInfoProfile] =
            mockOptionMethod()
        }

        mockOptionMethod.expects().returning(None).once()

        intercept[NoSuchElementException] {
          referenceTypeInfoProfile.classLoader
        }
      }
    }

    describe("#tryIndexedField") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, FieldVariableInfoProfile]

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def indexedField(name: String): FieldVariableInfoProfile =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[FieldVariableInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        referenceTypeInfoProfile.tryIndexedField(a1).get should be (r)
      }
    }

    describe("#indexedField") {
      it("should retrieve the value from indexedFieldOption") {
        val expected = mock[FieldVariableInfoProfile]
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfoProfile]]
        val name = "some name"

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def indexedFieldOption(name: String): Option[FieldVariableInfoProfile] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(Some(expected)).once()

        val actual = referenceTypeInfoProfile.indexedField(name)

        actual should be (expected)
      }

      it("should throw an exception if indexedFieldOption is None") {
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfoProfile]]
        val name = "some name"

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def indexedFieldOption(name: String): Option[FieldVariableInfoProfile] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(None).once()

        intercept[NoSuchElementException] {
          referenceTypeInfoProfile.indexedField(name)
        }
      }
    }

    describe("#tryIndexedVisibleFields") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FieldVariableInfoProfile]]

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def indexedVisibleFields: Seq[FieldVariableInfoProfile] =
            mockUnsafeMethod()
        }

        val a1 = "someName"
        val r = Seq(mock[FieldVariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        referenceTypeInfoProfile.tryIndexedVisibleFields.get should be (r)
      }
    }

    describe("#tryVisibleFields") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FieldVariableInfoProfile]]

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def visibleFields: Seq[FieldVariableInfoProfile] =
            mockUnsafeMethod()
        }

        val a1 = "someName"
        val r = Seq(mock[FieldVariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        referenceTypeInfoProfile.tryVisibleFields.get should be (r)
      }
    }

    describe("#tryAllFields") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FieldVariableInfoProfile]]

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def allFields: Seq[FieldVariableInfoProfile] =
            mockUnsafeMethod()
        }

        val a1 = "someName"
        val r = Seq(mock[FieldVariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        referenceTypeInfoProfile.tryAllFields.get should be (r)
      }
    }

    describe("#tryField") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, FieldVariableInfoProfile]

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def field(name: String): FieldVariableInfoProfile =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[FieldVariableInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        referenceTypeInfoProfile.tryField(a1).get should be (r)
      }
    }

    describe("#field") {
      it("should retrieve the value from fieldOption") {
        val expected = mock[FieldVariableInfoProfile]
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfoProfile]]
        val name = "some name"

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def fieldOption(name: String): Option[FieldVariableInfoProfile] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(Some(expected)).once()

        val actual = referenceTypeInfoProfile.field(name)

        actual should be (expected)
      }

      it("should throw an exception if fieldOption is None") {
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfoProfile]]
        val name = "some name"

        val referenceTypeInfoProfile = new TestReferenceTypeInfoProfile {
          override def fieldOption(name: String): Option[FieldVariableInfoProfile] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(None).once()

        intercept[NoSuchElementException] {
          referenceTypeInfoProfile.field(name)
        }
      }
    }
  }
}
