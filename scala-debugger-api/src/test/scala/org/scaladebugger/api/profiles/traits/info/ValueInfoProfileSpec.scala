package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestValueInfoProfile

import scala.util.Failure

class ValueInfoProfileSpec extends test.ParallelMockFunSpec
{
  describe("ValueInfoProfile") {
    describe("#toPrettyString") {
      it("should display null if the value is null") {
        val expected = "null"

        val valueInfoProfile = new TestValueInfoProfile {
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

        val valueInfoProfile = new TestValueInfoProfile {
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

        val mockStringInfoProfile = mock[StringInfoProfile]
        (mockStringInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
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
          override def toStringInfo: StringInfoProfile = mockStringInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the array instance if an array") {
        val expected = "ARRAY"

        val mockArrayInfoProfile = mock[ArrayInfoProfile]
        (mockArrayInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
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
          override def toArrayInfo: ArrayInfoProfile = mockArrayInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the thread instance if a thread") {
        val expected = "THREAD"

        val mockThreadInfoProfile = mock[ThreadInfoProfile]
        (mockThreadInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
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
          override def toThreadInfo: ThreadInfoProfile = mockThreadInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the thread group instance if a thread group") {
        val expected = "THREAD GROUP"

        val mockThreadGroupInfoProfile = mock[ThreadGroupInfoProfile]
        (mockThreadGroupInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
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
          override def toThreadGroupInfo: ThreadGroupInfoProfile =
            mockThreadGroupInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the class loader instance if a class loader") {
        val expected = "CLASS LOADER"

        val mockClassLoaderInfoProfile = mock[ClassLoaderInfoProfile]
        (mockClassLoaderInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
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
          override def toClassLoaderInfo: ClassLoaderInfoProfile =
            mockClassLoaderInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the class object instance if a class object") {
        val expected = "CLASS OBJECT"

        val mockClassObjectInfoProfile = mock[ClassObjectInfoProfile]
        (mockClassObjectInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
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
          override def toClassObjectInfo: ClassObjectInfoProfile =
            mockClassObjectInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the object instance if an object") {
        val expected = "OBJECT"

        val mockObjectInfoProfile = mock[ObjectInfoProfile]
        (mockObjectInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
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
          override def toObjectInfo: ObjectInfoProfile = mockObjectInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display the pretty string of the primitive instance if a primitive") {
        val expected = "PRIMITIVE"

        val mockPrimitiveInfoProfile = mock[PrimitiveInfoProfile]
        (mockPrimitiveInfoProfile.toPrettyString _).expects()
          .returning(expected).once()

        val valueInfoProfile = new TestValueInfoProfile {
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
          override def toPrimitiveInfo: PrimitiveInfoProfile = mockPrimitiveInfoProfile
        }

        val actual = valueInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display ??? if unrecognized value") {
        val expected = "???"

        val valueInfoProfile = new TestValueInfoProfile {
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

        val valueInfoProfile = new TestValueInfoProfile {
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
        val mockUnsafeMethod = mockFunction[PrimitiveInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toPrimitiveInfo: PrimitiveInfoProfile = mockUnsafeMethod()
        }

        val r = mock[PrimitiveInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToPrimitiveInfo.get should be (r)
      }
    }

    describe("#tryToObjectInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ObjectInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toObjectInfo: ObjectInfoProfile = mockUnsafeMethod()
        }

        val r = mock[ObjectInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToObjectInfo.get should be (r)
      }
    }

    describe("#tryToLocalValue") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Any]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toLocalValue: Any = mockUnsafeMethod()
        }

        val r = 3
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToLocalValue.get should be (r)
      }
    }

    describe("#tryToStringInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[StringInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toStringInfo: StringInfoProfile = mockUnsafeMethod()
        }

        val r = mock[StringInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToStringInfo.get should be (r)
      }
    }

    describe("#tryToArrayInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ArrayInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toArrayInfo: ArrayInfoProfile = mockUnsafeMethod()
        }

        val r = mock[ArrayInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToArrayInfo.get should be (r)
      }
    }

    describe("#tryToThreadInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toThreadInfo: ThreadInfoProfile = mockUnsafeMethod()
        }

        val r = mock[ThreadInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToThreadInfo.get should be (r)
      }
    }

    describe("#tryToThreadGroupInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ThreadGroupInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toThreadGroupInfo: ThreadGroupInfoProfile =
            mockUnsafeMethod()
        }

        val r = mock[ThreadGroupInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToThreadGroupInfo.get should be (r)
      }
    }

    describe("#tryToClassLoaderInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ClassLoaderInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toClassLoaderInfo: ClassLoaderInfoProfile =
            mockUnsafeMethod()
        }

        val r = mock[ClassLoaderInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToClassLoaderInfo.get should be (r)
      }
    }

    describe("#tryToClassObjectInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ClassObjectInfoProfile]

        val valueInfoProfile = new TestValueInfoProfile {
          override def toClassObjectInfo: ClassObjectInfoProfile =
            mockUnsafeMethod()
        }

        val r = mock[ClassObjectInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        valueInfoProfile.tryToClassObjectInfo.get should be (r)
      }
    }
  }
}
