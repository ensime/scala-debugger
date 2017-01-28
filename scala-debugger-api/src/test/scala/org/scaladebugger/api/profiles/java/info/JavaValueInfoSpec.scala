package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class JavaValueInfoSpec extends ParallelMockFunSpec
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockValue = mock[Value]
  private val mockNewPrimitiveProfile = mockFunction[PrimitiveValue, PrimitiveInfo]
  private val mockNewObjectProfile = mockFunction[ObjectReference, ObjectInfo]
  private val mockNewArrayProfile = mockFunction[ArrayReference, ArrayInfo]
  private val mockNewThreadProfile = mockFunction[ThreadReference, ThreadInfo]
  private val mockNewThreadGroupProfile = mockFunction[ThreadGroupReference, ThreadGroupInfo]
  private val mockNewClassObjectProfile = mockFunction[ClassObjectReference, ClassObjectInfo]
  private val mockNewClassLoaderProfile = mockFunction[ClassLoaderReference, ClassLoaderInfo]
  private val mockNewStringProfile = mockFunction[StringReference, StringInfo]

  describe("JavaValueInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ValueInfo]

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockValue
        )

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newValueInfo _)
          .expects(mockScalaVirtualMachine, mockValue)
          .returning(expected).once()

        val actual = javaValueInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockValue
        )

        val actual = javaValueInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockValue

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockValue
        )

        val actual = javaValueInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#typeInfo") {
      it("should supply a type info wrapper even if the value is null") {
        val expected = mock[TypeInfo]

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        ) {
          // NOTE: ScalaMock does not allow us to supply null to mock argument,
          //       so throwing an error if we aren't supplied with null
          override protected def newTypeProfile(_type: Type): TypeInfo = {
            require(_type == null)
            expected
          }
        }

        val actual = javaValueInfoProfile.`type`

        actual should be (expected)
      }

      it("should should return a new type info profile wrapping the type") {
        val expected = mock[TypeInfo]

        val mockType = mock[Type]
        (mockValue.`type` _).expects().returning(mockType).once()

        val mockNewTypeProfileFunction = mockFunction[Type, TypeInfo]
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockValue
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfo =
            mockNewTypeProfileFunction(_type)
        }

        mockNewTypeProfileFunction.expects(mockType).returning(expected).once()

        val actual = javaValueInfoProfile.`type`

        actual should be (expected)
      }
    }

    describe("#toArrayInfo") {
      it("should throw an assertion error if the value is null") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toArrayInfo
        }
      }

      it("should throw an assertion error if the value is not an array") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toArrayInfo
        }
      }

      it("should return an array reference wrapped in a profile") {
        val expected = mock[ArrayInfo]
        val mockArrayReference = mock[ArrayReference]

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockArrayReference
        ) {
          override protected def newArrayProfile(
            arrayReference: ArrayReference
          ): ArrayInfo = mockNewArrayProfile(arrayReference)
        }

        mockNewArrayProfile.expects(mockArrayReference)
          .returning(expected).once()

        val actual = javaValueInfoProfile.toArrayInfo

        actual should be (expected)
      }
    }

    describe("#toClassLoaderInfo") {
      it("should throw an assertion error if the value is null") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toClassLoaderInfo
        }
      }

      it("should throw an assertion error if the value is not an class loader") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toClassLoaderInfo
        }
      }

      it("should return an class loader reference wrapped in a profile") {
        val expected = mock[ClassLoaderInfo]
        val mockClassLoaderReference = mock[ClassLoaderReference]

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassLoaderReference
        ) {
          override protected def newClassLoaderProfile(
            classLoaderReference: ClassLoaderReference
          ): ClassLoaderInfo = mockNewClassLoaderProfile(classLoaderReference)
        }

        mockNewClassLoaderProfile.expects(mockClassLoaderReference)
          .returning(expected).once()

        val actual = javaValueInfoProfile.toClassLoaderInfo

        actual should be (expected)
      }
    }

    describe("#toClassObjectInfo") {
      it("should throw an assertion error if the value is null") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toClassObjectInfo
        }
      }

      it("should throw an assertion error if the value is not an class object") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toClassObjectInfo
        }
      }

      it("should return an class object reference wrapped in a profile") {
        val expected = mock[ClassObjectInfo]
        val mockClassObjectReference = mock[ClassObjectReference]

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassObjectReference
        ) {
          override protected def newClassObjectProfile(
            classObjectReference: ClassObjectReference
          ): ClassObjectInfo = mockNewClassObjectProfile(classObjectReference)
        }

        mockNewClassObjectProfile.expects(mockClassObjectReference)
          .returning(expected).once()

        val actual = javaValueInfoProfile.toClassObjectInfo

        actual should be (expected)
      }
    }

    describe("#toThreadGroupInfo") {
      it("should throw an assertion error if the value is null") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toThreadGroupInfo
        }
      }

      it("should throw an assertion error if the value is not an thread group") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toThreadGroupInfo
        }
      }

      it("should return an thread group reference wrapped in a profile") {
        val expected = mock[ThreadGroupInfo]
        val mockThreadGroupReference = mock[ThreadGroupReference]

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockThreadGroupReference
        ) {
          override protected def newThreadGroupProfile(
            threadGroupReference: ThreadGroupReference
          ): ThreadGroupInfo = mockNewThreadGroupProfile(threadGroupReference)
        }

        mockNewThreadGroupProfile.expects(mockThreadGroupReference)
          .returning(expected).once()

        val actual = javaValueInfoProfile.toThreadGroupInfo

        actual should be (expected)
      }
    }

    describe("#toThreadInfo") {
      it("should throw an assertion error if the value is null") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toThreadInfo
        }
      }

      it("should throw an assertion error if the value is not an thread") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toThreadInfo
        }
      }

      it("should return an thread reference wrapped in a profile") {
        val expected = mock[ThreadInfo]
        val mockThreadReference = mock[ThreadReference]

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockThreadReference
        ) {
          override protected def newThreadProfile(
            threadReference: ThreadReference
          ): ThreadInfo = mockNewThreadProfile(threadReference)
        }

        mockNewThreadProfile.expects(mockThreadReference)
          .returning(expected).once()

        val actual = javaValueInfoProfile.toThreadInfo

        actual should be (expected)
      }
    }

    describe("#toStringInfo") {
      it("should throw an assertion error if the value is null") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toStringInfo
        }
      }

      it("should throw an assertion error if the value is not a string") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toStringInfo
        }
      }

      it("should return a string reference wrapped in a profile") {
        val expected = mock[StringInfo]
        val mockStringReference = mock[StringReference]

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStringReference
        ) {
          override protected def newStringProfile(
            stringReference: StringReference
          ): StringInfo = mockNewStringProfile(stringReference)
        }

        mockNewStringProfile.expects(mockStringReference)
          .returning(expected).once()

        val actual = javaValueInfoProfile.toStringInfo

        actual should be (expected)
      }
    }

    describe("#toPrimitiveInfo") {
      it("should throw an assertion error if the value is null") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toPrimitiveInfo
        }
      }

      it("should throw an assertion error if the value is not an primitive") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toPrimitiveInfo
        }
      }

      it("should return a primitive value wrapped in a profile") {
        val expected = mock[PrimitiveInfo]
        val mockPrimitiveValue = mock[PrimitiveValue]

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockPrimitiveValue
        ) {
          override protected def newPrimitiveProfile(
            primitiveValue: PrimitiveValue
          ): PrimitiveInfo = mockNewPrimitiveProfile(primitiveValue)
        }

        mockNewPrimitiveProfile.expects(mockPrimitiveValue)
          .returning(expected).once()

        val actual = javaValueInfoProfile.toPrimitiveInfo

        actual should be (expected)
      }

      it("should return a void value wrapped in a profile") {
        val expected = mock[PrimitiveInfo]
        val mockVoidValue = mock[VoidValue]

        val mockNewPrimitiveProfile = mockFunction[VoidValue, PrimitiveInfo]
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockVoidValue
        ) {
          override protected def newPrimitiveProfile(
            voidValue: VoidValue
          ): PrimitiveInfo = mockNewPrimitiveProfile(voidValue)
        }

        mockNewPrimitiveProfile.expects(mockVoidValue)
          .returning(expected).once()

        val actual = javaValueInfoProfile.toPrimitiveInfo

        actual should be (expected)
      }
    }

    describe("#toObjectInfo") {
      it("should throw an assertion error if the value is null") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toObjectInfo
        }
      }

      it("should throw an assertion error if the value is not an object") {
        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          javaValueInfoProfile.toObjectInfo
        }
      }

      it("should return an object reference wrapped in a profile") {
        val expected = mock[ObjectInfo]
        val mockObjectReference = mock[ObjectReference]

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockObjectReference
        ) {
          override protected def newObjectProfile(
            arrayReference: ObjectReference
          ): ObjectInfo = mockNewObjectProfile(arrayReference)
        }

        mockNewObjectProfile.expects(mockObjectReference)
          .returning(expected).once()

        val actual = javaValueInfoProfile.toObjectInfo

        actual should be (expected)
      }
    }

    describe("#toLocalValue") {
      it("should return null if the value is null") {
        val expected: Any = null

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = javaValueInfoProfile.toLocalValue

        actual should be (expected)
      }

      it("should convert the remote value to its underlying value") {
        val expected = "some value"
        val mockStringReference = mock[StringReference]

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStringReference
        )

        (mockStringReference.value _).expects().returning(expected).once()
        val actual = javaValueInfoProfile.toLocalValue

        actual should be (expected)
      }
    }

    describe("#isPrimitive") {
      it("should return false if the value is null") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = javaValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return false if the value is not a primitive") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = javaValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return true if the value is a primitive") {
        val expected = true

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[PrimitiveValue]
        )

        val actual = javaValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return true if the value is void (considered primitive)") {
        val expected = true

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[VoidValue]
        )

        val actual = javaValueInfoProfile.isPrimitive

        actual should be (expected)
      }
    }

    describe("#isVoid") {
      it("should return false if the value is null") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = javaValueInfoProfile.isVoid

        actual should be (expected)
      }

      it("should return false if the value is not void") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = javaValueInfoProfile.isVoid

        actual should be (expected)
      }

      it("should return true if the value is void") {
        val expected = true

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[VoidValue]
        )

        val actual = javaValueInfoProfile.isVoid

        actual should be (expected)
      }
    }

    describe("#isObject") {
      it("should return false if the value is null") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = javaValueInfoProfile.isObject

        actual should be (expected)
      }

      it("should return false if the value is not a object") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = javaValueInfoProfile.isObject

        actual should be (expected)
      }

      it("should return true if the value is a object") {
        val expected = true

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[ObjectReference]
        )

        val actual = javaValueInfoProfile.isObject

        actual should be (expected)
      }
    }

    describe("#isString") {
      it("should return false if the value is null") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = javaValueInfoProfile.isString

        actual should be (expected)
      }

      it("should return false if the value is not a string") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = javaValueInfoProfile.isString

        actual should be (expected)
      }

      it("should return true if the value is a string") {
        val expected = true

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[StringReference]
        )

        val actual = javaValueInfoProfile.isString

        actual should be (expected)
      }
    }

    describe("#isArray") {
      it("should return false if the value is null") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = javaValueInfoProfile.isArray

        actual should be (expected)
      }

      it("should return false if the value is not a array") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = javaValueInfoProfile.isArray

        actual should be (expected)
      }

      it("should return true if the value is a array") {
        val expected = true

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[ArrayReference]
        )

        val actual = javaValueInfoProfile.isArray

        actual should be (expected)
      }
    }

    describe("#isClassLoader") {
      it("should return false if the value is null") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = javaValueInfoProfile.isClassLoader

        actual should be (expected)
      }

      it("should return false if the value is not a class loader") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = javaValueInfoProfile.isClassLoader

        actual should be (expected)
      }

      it("should return true if the value is a class loader") {
        val expected = true

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[ClassLoaderReference]
        )

        val actual = javaValueInfoProfile.isClassLoader

        actual should be (expected)
      }
    }

    describe("#isClassObject") {
      it("should return false if the value is null") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = javaValueInfoProfile.isClassObject

        actual should be (expected)
      }

      it("should return false if the value is not a class object") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = javaValueInfoProfile.isClassObject

        actual should be (expected)
      }

      it("should return true if the value is a class object") {
        val expected = true

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[ClassObjectReference]
        )

        val actual = javaValueInfoProfile.isClassObject

        actual should be (expected)
      }
    }

    describe("#isThreadGroup") {
      it("should return false if the value is null") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = javaValueInfoProfile.isThreadGroup

        actual should be (expected)
      }

      it("should return false if the value is not a thread group") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = javaValueInfoProfile.isThreadGroup

        actual should be (expected)
      }

      it("should return true if the value is a thread group") {
        val expected = true

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[ThreadGroupReference]
        )

        val actual = javaValueInfoProfile.isThreadGroup

        actual should be (expected)
      }
    }

    describe("#isThread") {
      it("should return false if the value is null") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = javaValueInfoProfile.isThread

        actual should be (expected)
      }

      it("should return false if the value is not a thread") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = javaValueInfoProfile.isThread

        actual should be (expected)
      }

      it("should return true if the value is a thread") {
        val expected = true

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[ThreadReference]
        )

        val actual = javaValueInfoProfile.isThread

        actual should be (expected)
      }
    }

    describe("#isNull") {
      it("should return true if the value is null") {
        val expected = true

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = javaValueInfoProfile.isNull

        actual should be (expected)
      }

      it("should return false if the value is not null") {
        val expected = false

        val javaValueInfoProfile = new JavaValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = javaValueInfoProfile.isNull

        actual should be (expected)
      }
    }
  }
}
