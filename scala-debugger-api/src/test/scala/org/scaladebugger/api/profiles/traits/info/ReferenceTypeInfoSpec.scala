package org.scaladebugger.api.profiles.traits.info

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestReferenceTypeInfo

import scala.util.{Success, Try}

class ReferenceTypeInfoSpec extends ParallelMockFunSpec
{
  describe("ReferenceTypeInfo") {
    describe("#toPrettyString") {
      it("should display the reference type name") {
        val expected = "some.class.name"

        val referenceTypeInfoProfile = new TestReferenceTypeInfo {
          override def name: String = "some.class.name"
        }

        val actual = referenceTypeInfoProfile.toPrettyString

        actual should be(expected)
      }
    }

    describe("#classLoader") {
      it("should retrieve the value from classLoaderOption") {
        val expected = mock[ClassLoaderInfo]
        val mockOptionMethod = mockFunction[Option[ClassLoaderInfo]]

        val referenceTypeInfoProfile = new TestReferenceTypeInfo {
          override def classLoaderOption: Option[ClassLoaderInfo] =
            mockOptionMethod()
        }

        mockOptionMethod.expects().returning(Some(expected)).once()

        val actual = referenceTypeInfoProfile.classLoader

        actual should be (expected)
      }

      it("should throw an exception if classLoaderOption is None") {
        val mockOptionMethod = mockFunction[Option[ClassLoaderInfo]]

        val referenceTypeInfoProfile = new TestReferenceTypeInfo {
          override def classLoaderOption: Option[ClassLoaderInfo] =
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
        val mockUnsafeMethod = mockFunction[String, FieldVariableInfo]

        val referenceTypeInfoProfile = new TestReferenceTypeInfo {
          override def indexedField(name: String): FieldVariableInfo =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[FieldVariableInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        referenceTypeInfoProfile.tryIndexedField(a1).get should be (r)
      }
    }

    describe("#indexedField") {
      it("should retrieve the value from indexedFieldOption") {
        val expected = mock[FieldVariableInfo]
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfo]]
        val name = "some name"

        val referenceTypeInfoProfile = new TestReferenceTypeInfo {
          override def indexedFieldOption(name: String): Option[FieldVariableInfo] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(Some(expected)).once()

        val actual = referenceTypeInfoProfile.indexedField(name)

        actual should be (expected)
      }

      it("should throw an exception if indexedFieldOption is None") {
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfo]]
        val name = "some name"

        val referenceTypeInfoProfile = new TestReferenceTypeInfo {
          override def indexedFieldOption(name: String): Option[FieldVariableInfo] =
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
        val mockUnsafeMethod = mockFunction[Seq[FieldVariableInfo]]

        val referenceTypeInfoProfile = new TestReferenceTypeInfo {
          override def indexedVisibleFields: Seq[FieldVariableInfo] =
            mockUnsafeMethod()
        }

        val a1 = "someName"
        val r = Seq(mock[FieldVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        referenceTypeInfoProfile.tryIndexedVisibleFields.get should be (r)
      }
    }

    describe("#tryVisibleFields") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FieldVariableInfo]]

        val referenceTypeInfoProfile = new TestReferenceTypeInfo {
          override def visibleFields: Seq[FieldVariableInfo] =
            mockUnsafeMethod()
        }

        val a1 = "someName"
        val r = Seq(mock[FieldVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        referenceTypeInfoProfile.tryVisibleFields.get should be (r)
      }
    }

    describe("#tryAllFields") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FieldVariableInfo]]

        val referenceTypeInfoProfile = new TestReferenceTypeInfo {
          override def allFields: Seq[FieldVariableInfo] =
            mockUnsafeMethod()
        }

        val a1 = "someName"
        val r = Seq(mock[FieldVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        referenceTypeInfoProfile.tryAllFields.get should be (r)
      }
    }

    describe("#tryField") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, FieldVariableInfo]

        val referenceTypeInfoProfile = new TestReferenceTypeInfo {
          override def field(name: String): FieldVariableInfo =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[FieldVariableInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        referenceTypeInfoProfile.tryField(a1).get should be (r)
      }
    }

    describe("#field") {
      it("should retrieve the value from fieldOption") {
        val expected = mock[FieldVariableInfo]
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfo]]
        val name = "some name"

        val referenceTypeInfoProfile = new TestReferenceTypeInfo {
          override def fieldOption(name: String): Option[FieldVariableInfo] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(Some(expected)).once()

        val actual = referenceTypeInfoProfile.field(name)

        actual should be (expected)
      }

      it("should throw an exception if fieldOption is None") {
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfo]]
        val name = "some name"

        val referenceTypeInfoProfile = new TestReferenceTypeInfo {
          override def fieldOption(name: String): Option[FieldVariableInfo] =
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
