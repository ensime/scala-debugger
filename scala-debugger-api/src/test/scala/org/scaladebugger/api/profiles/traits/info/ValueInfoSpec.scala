package org.scaladebugger.api.profiles.traits.info

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestValueInfo

import scala.util.Failure

class ValueInfoSpec extends ParallelMockFunSpec
{
  describe("ValueInfo") {
    describe("#toPrettyString") {
      it("should display null if the value is null") {
        val expected = "null"

        val valueInfoProfile = new TestValueInfo {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = false
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = true
          override def isThreadGroup: Boolean = false
          override def isThread: Boolean = false
          override def isClassLoader: Boolean = false
          override def isClassObject: Boolean = false
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display void if the value is void") {
        val expected = "void"

        val valueInfoProfile = new TestValueInfo {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = false
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = true
          override def isNull: Boolean = false
          override def isThreadGroup: Boolean = false
          override def isThread: Boolean = false
          override def isClassLoader: Boolean = false
          override def isClassObject: Boolean = false
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the string instnace if a string") {
        val expected = "STRING"

        val mockStringInfoProfile = mock[StringInfo]
        (mockStringInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfo {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = true
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def isThreadGroup: Boolean = false
          override def isThread: Boolean = false
          override def isClassLoader: Boolean = false
          override def isClassObject: Boolean = false
          override def toStringInfo: StringInfo = mockStringInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the array instance if an array") {
        val expected = "ARRAY"

        val mockArrayInfoProfile = mock[ArrayInfo]
        (mockArrayInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfo {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = false
          override def isArray: Boolean = true
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def isThreadGroup: Boolean = false
          override def isThread: Boolean = false
          override def isClassLoader: Boolean = false
          override def isClassObject: Boolean = false
          override def toArrayInfo: ArrayInfo = mockArrayInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the thread instance if a thread") {
        val expected = "THREAD"

        val mockThreadInfoProfile = mock[ThreadInfo]
        (mockThreadInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfo {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def isThreadGroup: Boolean = false
          override def isThread: Boolean = true
          override def isClassLoader: Boolean = false
          override def isClassObject: Boolean = false
          override def toThreadInfo: ThreadInfo = mockThreadInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the thread group instance if a thread group") {
        val expected = "THREAD GROUP"

        val mockThreadGroupInfoProfile = mock[ThreadGroupInfo]
        (mockThreadGroupInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfo {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def isThreadGroup: Boolean = true
          override def isThread: Boolean = false
          override def isClassLoader: Boolean = false
          override def isClassObject: Boolean = false
          override def toThreadGroupInfo: ThreadGroupInfo =
            mockThreadGroupInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the class loader instance if a class loader") {
        val expected = "CLASS LOADER"

        val mockClassLoaderInfoProfile = mock[ClassLoaderInfo]
        (mockClassLoaderInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfo {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def isThreadGroup: Boolean = false
          override def isThread: Boolean = false
          override def isClassLoader: Boolean = true
          override def isClassObject: Boolean = false
          override def toClassLoaderInfo: ClassLoaderInfo =
            mockClassLoaderInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the class object instance if a class object") {
        val expected = "CLASS OBJECT"

        val mockClassObjectInfoProfile = mock[ClassObjectInfo]
        (mockClassObjectInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfo {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def isThreadGroup: Boolean = false
          override def isThread: Boolean = false
          override def isClassLoader: Boolean = false
          override def isClassObject: Boolean = true
          override def toClassObjectInfo: ClassObjectInfo =
            mockClassObjectInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the object instance if an object") {
        val expected = "OBJECT"

        val mockObjectInfoProfile = mock[ObjectInfo]
        (mockObjectInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfo {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = true
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def isThreadGroup: Boolean = false
          override def isThread: Boolean = false
          override def isClassLoader: Boolean = false
          override def isClassObject: Boolean = false
          override def toObjectInfo: ObjectInfo = mockObjectInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the primitive instance if a primitive") {
        val expected = "PRIMITIVE"

        val mockPrimitiveInfoProfile = mock[PrimitiveInfo]
        (mockPrimitiveInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfo {
          override def isPrimitive: Boolean = true
          override def isObject: Boolean = false
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def isThreadGroup: Boolean = false
          override def isThread: Boolean = false
          override def isClassLoader: Boolean = false
          override def isClassObject: Boolean = false
          override def toPrimitiveInfo: PrimitiveInfo = mockPrimitiveInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display ??? if unrecognized value") {
        val expected = "???"

        val valueInfoProfile = new TestValueInfo {
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = false
          override def isString: Boolean = false
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def isThreadGroup: Boolean = false
          override def isThread: Boolean = false
          override def isClassLoader: Boolean = false
          override def isClassObject: Boolean = false
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display <ERROR> if failed to retrieve value") {
        val expected = "<ERROR>"

        val valueInfoProfile = new TestValueInfo {
          override def toLocalValue: Any = throw new Throwable
          override def isPrimitive: Boolean = false
          override def isObject: Boolean = false
          override def isString: Boolean = true
          override def isArray: Boolean = false
          override def isVoid: Boolean = false
          override def isNull: Boolean = false
          override def isThreadGroup: Boolean = false
          override def isThread: Boolean = false
          override def isClassLoader: Boolean = false
          override def isClassObject: Boolean = false
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }
    }

    describe("#tryToPrimitiveInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[PrimitiveInfo]

        val valueInfoProfile = new TestValueInfo {
          override def toPrimitiveInfo: PrimitiveInfo = mockUnsafeMethod()
        }

        val r = mock[PrimitiveInfo]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToPrimitiveInfo.get should be (r)
      }
    }

    describe("#tryToObjectInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ObjectInfo]

        val valueInfoProfile = new TestValueInfo {
          override def toObjectInfo: ObjectInfo = mockUnsafeMethod()
        }

        val r = mock[ObjectInfo]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToObjectInfo.get should be (r)
      }
    }

    describe("#tryToLocalValue") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Any]

        val valueInfoProfile = new TestValueInfo {
          override def toLocalValue: Any = mockUnsafeMethod()
        }

        val r = 3
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToLocalValue.get should be (r)
      }
    }

    describe("#tryToStringInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[StringInfo]

        val valueInfoProfile = new TestValueInfo {
          override def toStringInfo: StringInfo = mockUnsafeMethod()
        }

        val r = mock[StringInfo]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToStringInfo.get should be (r)
      }
    }

    describe("#tryToArrayInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ArrayInfo]

        val valueInfoProfile = new TestValueInfo {
          override def toArrayInfo: ArrayInfo = mockUnsafeMethod()
        }

        val r = mock[ArrayInfo]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToArrayInfo.get should be (r)
      }
    }

    describe("#tryToThreadInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadInfo]

        val valueInfoProfile = new TestValueInfo {
          override def toThreadInfo: ThreadInfo = mockUnsafeMethod()
        }

        val r = mock[ThreadInfo]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToThreadInfo.get should be (r)
      }
    }

    describe("#tryToThreadGroupInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadGroupInfo]

        val valueInfoProfile = new TestValueInfo {
          override def toThreadGroupInfo: ThreadGroupInfo =
            mockUnsafeMethod()
        }

        val r = mock[ThreadGroupInfo]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToThreadGroupInfo.get should be (r)
      }
    }

    describe("#tryToClassLoaderInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ClassLoaderInfo]

        val valueInfoProfile = new TestValueInfo {
          override def toClassLoaderInfo: ClassLoaderInfo =
            mockUnsafeMethod()
        }

        val r = mock[ClassLoaderInfo]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToClassLoaderInfo.get should be (r)
      }
    }

    describe("#tryToClassObjectInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ClassObjectInfo]

        val valueInfoProfile = new TestValueInfo {
          override def toClassObjectInfo: ClassObjectInfo =
            mockUnsafeMethod()
        }

        val r = mock[ClassObjectInfo]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToClassObjectInfo.get should be (r)
      }
    }
  }
}
