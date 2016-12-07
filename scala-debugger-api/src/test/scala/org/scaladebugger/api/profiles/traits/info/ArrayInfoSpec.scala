package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestArrayInfo

import scala.util.{Failure, Success, Try}

class ArrayInfoSpec extends test.ParallelMockFunSpec
{
  describe("ArrayInfo") {
    describe("#toPrettyString") {
      it("should support empty arrays") {
        val expected = "Array(length = 0)[<EMPTY>]"

        val index = 0
        val maxLength = 10
        val r: Try[Seq[ValueInfo]] = Success(Nil)

        val mockUnsafeMethod = mockFunction[Int, Int, Try[Seq[ValueInfo]]]
        mockUnsafeMethod.expects(index, maxLength).returning(r).once()

        val arrayInfoProfile = new TestArrayInfo {
          override def length: Int = r.get.length

          override def tryValues(
            index: Int,
            totalElements: Int
          ): Try[Seq[ValueInfo]] = mockUnsafeMethod(index, totalElements)
        }

        val actual = arrayInfoProfile.toPrettyString(maxLength)

        actual should be (expected)
      }

      it("should support arrays with length less than or equal to the maximum") {
        val expected = "Array(length = 2)[test1,test2]"

        val index = 0
        val maxLength = 2
        val r = Success((1 to maxLength).map(_ => mock[ValueInfo]))
        r.get.zipWithIndex.foreach { case (v, i) =>
          (v.toPrettyString _).expects().returning(s"test${i+1}").once()
        }

        val mockUnsafeMethod = mockFunction[Int, Int, Try[Seq[ValueInfo]]]
        mockUnsafeMethod.expects(index, maxLength).returning(r).once()

        val arrayInfoProfile = new TestArrayInfo {
          override def length: Int = r.get.length

          override def tryValues(
            index: Int,
            totalElements: Int
          ): Try[Seq[ValueInfo]] = mockUnsafeMethod(index, totalElements)
        }

        val actual = arrayInfoProfile.toPrettyString(maxLength)

        actual should be (expected)
      }

      it("should cut off additional values from arrays with length over the maximum") {
        val expected = "Array(length = 3)[test1,test2,...]"

        val index = 0
        val maxLength = 2
        val r = Success((1 to maxLength).map(_ => mock[ValueInfo]))
        r.get.zipWithIndex.foreach { case (v, i) =>
          (v.toPrettyString _).expects().returning(s"test${i+1}").once()
        }

        val mockUnsafeMethod = mockFunction[Int, Int, Try[Seq[ValueInfo]]]
        mockUnsafeMethod.expects(index, maxLength).returning(r).once()

        val arrayInfoProfile = new TestArrayInfo {
          override def length: Int = r.get.length + 1

          override def tryValues(
            index: Int,
            totalElements: Int
          ): Try[Seq[ValueInfo]] = mockUnsafeMethod(index, totalElements)
        }

        val actual = arrayInfoProfile.toPrettyString(maxLength)

        actual should be (expected)
      }

      it("should return no values if failing to get array values") {
        val expected = "Array(length = 3)[<ERROR>]"

        val index = 0
        val maxLength = 3
        val r = Failure(new Throwable)

        val mockUnsafeMethod = mockFunction[Int, Int, Try[Seq[ValueInfo]]]
        mockUnsafeMethod.expects(index, maxLength).returning(r).once()

        val arrayInfoProfile = new TestArrayInfo {
          override def length: Int = maxLength

          override def tryValues(
            index: Int,
            totalElements: Int
          ): Try[Seq[ValueInfo]] = mockUnsafeMethod(index, totalElements)
        }

        val actual = arrayInfoProfile.toPrettyString(maxLength)

        actual should be (expected)
      }
    }

    describe("#tryValue") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, ValueInfo]

        val arrayInfoProfile = new TestArrayInfo {
          override def value(index: Int): ValueInfo =
            mockUnsafeMethod(index)
        }

        val a1 = 999
        val r = mock[ValueInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        arrayInfoProfile.tryValue(a1).get should be (r)
      }
    }

    describe("#apply") {
      it("should invoke the unsafe value method") {
        val mockUnsafeMethod = mockFunction[Int, ValueInfo]

        val arrayInfoProfile = new TestArrayInfo {
          override def value(index: Int): ValueInfo =
            mockUnsafeMethod(index)
        }

        val a1 = 999
        val r = mock[ValueInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        arrayInfoProfile(a1) should be (r)
      }
    }

    describe("#trySetValueFromInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, ValueInfo, ValueInfo]

        val variableInfoProfile = new TestArrayInfo {
          override def setValueFromInfo(
            index: Int, valueInfo: ValueInfo
          ): ValueInfo = mockUnsafeMethod(index, valueInfo)
        }

        val a1 = 999
        val a2 = mock[ValueInfo]
        val r = mock[ValueInfo]
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        variableInfoProfile.trySetValueFromInfo(a1, a2).get should be (r)
      }
    }

    describe("#trySetValue") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, Any, ValueInfo]

        val arrayInfoProfile = new TestArrayInfo {
          override def setValue[T](
            index: Int,
            value: T
          )(implicit typeTag: scala.reflect.runtime.universe.TypeTag[T]): ValueInfo =
            mockUnsafeMethod(index, value)
        }

        val a1 = 999
        val a2 = "value"
        val r = mock[ValueInfo]
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        arrayInfoProfile.trySetValue(a1, a2).get should be (r)
      }
    }

    describe("#setValue") {
      it("should throw an exception if the value is not an AnyVal or String") {
        val arrayInfoProfile = new TestArrayInfo

        intercept[UnsupportedTypeException] {
          arrayInfoProfile.setValue(0, new AnyRef)
        }
      }

      it("should convert the AnyVal value to a remote value and set it") {
        val mockCreateRemotely = mockFunction[AnyVal, ValueInfo]
        val mockSetValueFromInfo = mockFunction[Int, ValueInfo, ValueInfo]
        val arrayInfoProfile = new TestArrayInfo {
          override def createRemotely(value: AnyVal): ValueInfo =
            mockCreateRemotely(value)

          override def setValueFromInfo(
            index: Int,
            value: ValueInfo
          ): ValueInfo = mockSetValueFromInfo(index, value)
        }

        val a1 = 999
        val a2: AnyVal = 33
        val r = mock[ValueInfo]
        mockCreateRemotely.expects(a2).returning(r).once()
        mockSetValueFromInfo.expects(a1, r).returning(r).once()

        arrayInfoProfile.setValue(a1, a2) should be (r)
      }

      it("should convert the String value to a remote value and set it") {
        val mockCreateRemotely = mockFunction[String, ValueInfo]
        val mockSetValueFromInfo = mockFunction[Int, ValueInfo, ValueInfo]
        val arrayInfoProfile = new TestArrayInfo {
          override def createRemotely(value: String): ValueInfo =
            mockCreateRemotely(value)

          override def setValueFromInfo(
            index: Int,
            value: ValueInfo
          ): ValueInfo = mockSetValueFromInfo(index, value)
        }

        val a1 = 999
        val a2 = "some string"
        val r = mock[ValueInfo]
        mockCreateRemotely.expects(a2).returning(r).once()
        mockSetValueFromInfo.expects(a1, r).returning(r).once()

        arrayInfoProfile.setValue(a1, a2) should be (r)
      }
    }

    describe("#update") {
      it("should invoke the unsafe setValue method") {
        val mockUnsafeMethod = mockFunction[Int, Any, ValueInfo]

        val arrayInfoProfile = new TestArrayInfo {
          override def setValue[T](
            index: Int,
            value: T
          )(implicit typeTag: scala.reflect.runtime.universe.TypeTag[T]): ValueInfo =
            mockUnsafeMethod(index, value)
        }

        val a1 = 999
        val a2 = "value"
        val r = mock[ValueInfo]
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        (arrayInfoProfile(a1) = a2) should be (r)
      }
    }

    describe("#tryValues(index, totalElements)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, Int, Seq[ValueInfo]]

        val arrayInfoProfile = new TestArrayInfo {
          override def values(index: Int, totalElements: Int): Seq[ValueInfo] =
            mockUnsafeMethod(index, totalElements)
        }

        val a1 = 999
        val a2 = 40
        val r = Seq(mock[ValueInfo])
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        arrayInfoProfile.tryValues(a1, a2).get should be (r)
      }
    }

    describe("#tryValues") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ValueInfo]]

        val arrayInfoProfile = new TestArrayInfo {
          override def values: Seq[ValueInfo] = mockUnsafeMethod()
        }

        val r = Seq(mock[ValueInfo])
        mockUnsafeMethod.expects().returning(r).once()
        arrayInfoProfile.tryValues.get should be (r)
      }
    }

    describe("#trySetValuesFromInfo(index, values, srcIndex, totalElements)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, Seq[ValueInfo], Int, Int, Seq[ValueInfo]]

        val variableInfoProfile = new TestArrayInfo {
          override def setValuesFromInfo(
            index: Int, values: Seq[ValueInfo],
            srcIndex: Int, totalElements: Int
          ): Seq[ValueInfo] = mockUnsafeMethod(index, values, srcIndex, totalElements)
        }

        val a1 = 999
        val a2 = Seq(mock[ValueInfo])
        val a3 = 1
        val a4 = 2
        val r = a2
        mockUnsafeMethod.expects(a1, a2, a3, a4).returning(r).once()
        variableInfoProfile.trySetValuesFromInfo(a1, a2, a3, a4).get should be (r)
      }
    }

    describe("#trySetValues(index, values, srcIndex, totalElements)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Int, Seq[Any], Int, Int, Seq[ValueInfo]]

        val arrayInfoProfile = new TestArrayInfo {
          override def setValues[T](
            index: Int,
            values: Seq[T],
            srcIndex: Int,
            totalElements: Int
          )(implicit typeTag: scala.reflect.runtime.universe.TypeTag[T]): Seq[ValueInfo] =
            mockUnsafeMethod(index, values, srcIndex, totalElements)
        }

        val a1 = 999
        val a2 = Seq(1, 2, "test")
        val a3 = 40
        val a4 = 55
        val r = Seq(mock[ValueInfo])
        mockUnsafeMethod.expects(a1, a2, a3, a4).returning(r).once()
        arrayInfoProfile.trySetValues(a1, a2, a3, a4).get should be (r)
      }
    }

    describe("#setValues(index, values, srcIndex, totalElements)") {
      it("should throw an exception if a value is not an AnyVal or String") {
        val arrayInfoProfile = new TestArrayInfo

        intercept[UnsupportedTypeException] {
          arrayInfoProfile.setValues(0, Seq(new AnyRef), 0, 1)
        }
      }

      it("should convert the local AnyVal values to a remote values and set them") {
        val mockCreateRemotely = mockFunction[AnyVal, ValueInfo]
        val mockSetValuesFromInfo =
          mockFunction[Int, Seq[ValueInfo], Int, Int, Seq[ValueInfo]]
        val arrayInfoProfile = new TestArrayInfo {
          override def createRemotely(value: AnyVal): ValueInfo =
            mockCreateRemotely(value)

          override def setValuesFromInfo(
            index: Int,
            values: Seq[ValueInfo],
            srcIndex: Int,
            totalElements: Int
          ): Seq[ValueInfo] = mockSetValuesFromInfo(
            index, values, srcIndex, totalElements
          )
        }

        val a1 = 999
        val a2: Seq[AnyVal] = Seq(3, true)
        val a3 = 1
        val a4 = 2
        val r = a2.map(_ => mock[ValueInfo])

        r.zip(a2).foreach { case (r, a) =>
          mockCreateRemotely.expects(a).returning(r).once()
        }
        mockSetValuesFromInfo.expects(a1, r, a3, a4).returning(r).once()

        arrayInfoProfile.setValues(a1, a2, a3, a4) should be (r)
      }

      it("should convert the local String values to a remote values and set them") {
        val mockCreateRemotely = mockFunction[String, ValueInfo]
        val mockSetValuesFromInfo =
          mockFunction[Int, Seq[ValueInfo], Int, Int, Seq[ValueInfo]]
        val arrayInfoProfile = new TestArrayInfo {
          override def createRemotely(value: String): ValueInfo =
            mockCreateRemotely(value)

          override def setValuesFromInfo(
            index: Int,
            values: Seq[ValueInfo],
            srcIndex: Int,
            totalElements: Int
          ): Seq[ValueInfo] = mockSetValuesFromInfo(
            index, values, srcIndex, totalElements
          )
        }

        val a1 = 999
        val a2: Seq[String] = Seq("one", "two")
        val a3 = 1
        val a4 = 2
        val r = a2.map(_ => mock[ValueInfo])

        r.zip(a2).foreach { case (r, a) =>
          mockCreateRemotely.expects(a).returning(r).once()
        }
        mockSetValuesFromInfo.expects(a1, r, a3, a4).returning(r).once()

        arrayInfoProfile.setValues(a1, a2, a3, a4) should be (r)
      }
    }

    describe("#trySetValuesFromInfo(values)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[ValueInfo], Seq[ValueInfo]]

        val variableInfoProfile = new TestArrayInfo {
          override def setValuesFromInfo(
            values: Seq[ValueInfo]
          ): Seq[ValueInfo] = mockUnsafeMethod(values)
        }

        val a1 = Seq(mock[ValueInfo])
        val r = a1
        mockUnsafeMethod.expects(a1).returning(r).once()
        variableInfoProfile.trySetValuesFromInfo(a1).get should be (r)
      }
    }

    describe("#trySetValues(values)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[Any], Seq[ValueInfo]]

        val arrayInfoProfile = new TestArrayInfo {
          override def setValues[T](
            values: Seq[T]
          )(implicit typeTag: scala.reflect.runtime.universe.TypeTag[T]): Seq[ValueInfo] =
            mockUnsafeMethod(values)
        }

        val a1 = Seq(1, 2, "test")
        val r = Seq(mock[ValueInfo])
        mockUnsafeMethod.expects(a1).returning(r).once()
        arrayInfoProfile.trySetValues(a1).get should be (r)
      }
    }

    describe("#setValues(values)") {
      it("should throw an exception if a value is not an AnyVal or String") {
        val arrayInfoProfile = new TestArrayInfo

        intercept[UnsupportedTypeException] {
          arrayInfoProfile.setValues(Seq(new AnyRef))
        }
      }

      it("should convert the local AnyVal values to a remote values and set them") {
        val mockCreateRemotely = mockFunction[AnyVal, ValueInfo]
        val mockSetValuesFromInfo = mockFunction[Seq[ValueInfo], Seq[ValueInfo]]
        val arrayInfoProfile = new TestArrayInfo {
          override def createRemotely(value: AnyVal): ValueInfo =
            mockCreateRemotely(value)

          override def setValuesFromInfo(
            values: Seq[ValueInfo]
          ): Seq[ValueInfo] = mockSetValuesFromInfo(values)
        }

        val a1: Seq[AnyVal] = Seq(3, true)
        val r = a1.map(_ => mock[ValueInfo])

        r.zip(a1).foreach { case (r, a) =>
          mockCreateRemotely.expects(a).returning(r).once()
        }
        mockSetValuesFromInfo.expects(r).returning(r).once()

        arrayInfoProfile.setValues(a1) should be (r)
      }

      it("should convert the local String values to a remote values and set them") {
        val mockCreateRemotely = mockFunction[String, ValueInfo]
        val mockSetValuesFromInfo = mockFunction[Seq[ValueInfo], Seq[ValueInfo]]
        val arrayInfoProfile = new TestArrayInfo {
          override def createRemotely(value: String): ValueInfo =
            mockCreateRemotely(value)

          override def setValuesFromInfo(
            values: Seq[ValueInfo]
          ): Seq[ValueInfo] = mockSetValuesFromInfo(values)
        }

        val a1 = Seq("one", "two")
        val r = a1.map(_ => mock[ValueInfo])

        r.zip(a1).foreach { case (r, a) =>
          mockCreateRemotely.expects(a).returning(r).once()
        }
        mockSetValuesFromInfo.expects(r).returning(r).once()

        arrayInfoProfile.setValues(a1) should be (r)
      }
    }
  }
}
