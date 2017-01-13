package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureValueInfoSpec extends ParallelMockFunSpec
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

  describe("PureValueInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ValueInfo]

        val pureValueInfoProfile = new PureValueInfo(
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

        val actual = pureValueInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockValue
        )

        val actual = pureValueInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockValue

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockValue
        )

        val actual = pureValueInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#typeInfo") {
      it("should supply a type info wrapper even if the value is null") {
        val expected = mock[TypeInfo]

        val pureValueInfoProfile = new PureValueInfo(
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

        val actual = pureValueInfoProfile.typeInfo

        actual should be (expected)
      }

      it("should should return a new type info profile wrapping the type") {
        val expected = mock[TypeInfo]

        val mockType = mock[Type]
        (mockValue.`type` _).expects().returning(mockType).once()

        val mockNewTypeProfileFunction = mockFunction[Type, TypeInfo]
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockValue
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfo =
            mockNewTypeProfileFunction(_type)
        }

        mockNewTypeProfileFunction.expects(mockType).returning(expected).once()

        val actual = pureValueInfoProfile.typeInfo

        actual should be (expected)
      }
    }

    describe("#toArrayInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toArrayInfo
        }
      }

      it("should throw an assertion error if the value is not an array") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toArrayInfo
        }
      }

      it("should return an array reference wrapped in a profile") {
        val expected = mock[ArrayInfo]
        val mockArrayReference = mock[ArrayReference]

        val pureValueInfoProfile = new PureValueInfo(
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

        val actual = pureValueInfoProfile.toArrayInfo

        actual should be (expected)
      }
    }

    describe("#toClassLoaderInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toClassLoaderInfo
        }
      }

      it("should throw an assertion error if the value is not an class loader") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toClassLoaderInfo
        }
      }

      it("should return an class loader reference wrapped in a profile") {
        val expected = mock[ClassLoaderInfo]
        val mockClassLoaderReference = mock[ClassLoaderReference]

        val pureValueInfoProfile = new PureValueInfo(
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

        val actual = pureValueInfoProfile.toClassLoaderInfo

        actual should be (expected)
      }
    }

    describe("#toClassObjectInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toClassObjectInfo
        }
      }

      it("should throw an assertion error if the value is not an class object") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toClassObjectInfo
        }
      }

      it("should return an class object reference wrapped in a profile") {
        val expected = mock[ClassObjectInfo]
        val mockClassObjectReference = mock[ClassObjectReference]

        val pureValueInfoProfile = new PureValueInfo(
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

        val actual = pureValueInfoProfile.toClassObjectInfo

        actual should be (expected)
      }
    }

    describe("#toThreadGroupInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toThreadGroupInfo
        }
      }

      it("should throw an assertion error if the value is not an thread group") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toThreadGroupInfo
        }
      }

      it("should return an thread group reference wrapped in a profile") {
        val expected = mock[ThreadGroupInfo]
        val mockThreadGroupReference = mock[ThreadGroupReference]

        val pureValueInfoProfile = new PureValueInfo(
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

        val actual = pureValueInfoProfile.toThreadGroupInfo

        actual should be (expected)
      }
    }

    describe("#toThreadInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toThreadInfo
        }
      }

      it("should throw an assertion error if the value is not an thread") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toThreadInfo
        }
      }

      it("should return an thread reference wrapped in a profile") {
        val expected = mock[ThreadInfo]
        val mockThreadReference = mock[ThreadReference]

        val pureValueInfoProfile = new PureValueInfo(
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

        val actual = pureValueInfoProfile.toThreadInfo

        actual should be (expected)
      }
    }

    describe("#toStringInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toStringInfo
        }
      }

      it("should throw an assertion error if the value is not a string") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toStringInfo
        }
      }

      it("should return a string reference wrapped in a profile") {
        val expected = mock[StringInfo]
        val mockStringReference = mock[StringReference]

        val pureValueInfoProfile = new PureValueInfo(
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

        val actual = pureValueInfoProfile.toStringInfo

        actual should be (expected)
      }
    }

    describe("#toPrimitiveInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toPrimitiveInfo
        }
      }

      it("should throw an assertion error if the value is not an primitive") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toPrimitiveInfo
        }
      }

      it("should return a primitive value wrapped in a profile") {
        val expected = mock[PrimitiveInfo]
        val mockPrimitiveValue = mock[PrimitiveValue]

        val pureValueInfoProfile = new PureValueInfo(
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

        val actual = pureValueInfoProfile.toPrimitiveInfo

        actual should be (expected)
      }

      it("should return a void value wrapped in a profile") {
        val expected = mock[PrimitiveInfo]
        val mockVoidValue = mock[VoidValue]

        val mockNewPrimitiveProfile = mockFunction[VoidValue, PrimitiveInfo]
        val pureValueInfoProfile = new PureValueInfo(
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

        val actual = pureValueInfoProfile.toPrimitiveInfo

        actual should be (expected)
      }
    }

    describe("#toObjectInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toObjectInfo
        }
      }

      it("should throw an assertion error if the value is not an object") {
        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toObjectInfo
        }
      }

      it("should return an object reference wrapped in a profile") {
        val expected = mock[ObjectInfo]
        val mockObjectReference = mock[ObjectReference]

        val pureValueInfoProfile = new PureValueInfo(
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

        val actual = pureValueInfoProfile.toObjectInfo

        actual should be (expected)
      }
    }

    describe("#toLocalValue") {
      it("should return null if the value is null") {
        val expected: Any = null

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.toLocalValue

        actual should be (expected)
      }

      it("should convert the remote value to its underlying value") {
        val expected = "some value"
        val mockStringReference = mock[StringReference]

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStringReference
        )

        (mockStringReference.value _).expects().returning(expected).once()
        val actual = pureValueInfoProfile.toLocalValue

        actual should be (expected)
      }
    }

    describe("#isPrimitive") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return false if the value is not a primitive") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return true if the value is a primitive") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[PrimitiveValue]
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return true if the value is void (considered primitive)") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[VoidValue]
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }
    }

    describe("#isVoid") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isVoid

        actual should be (expected)
      }

      it("should return false if the value is not void") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isVoid

        actual should be (expected)
      }

      it("should return true if the value is void") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[VoidValue]
        )

        val actual = pureValueInfoProfile.isVoid

        actual should be (expected)
      }
    }

    describe("#isObject") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isObject

        actual should be (expected)
      }

      it("should return false if the value is not a object") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isObject

        actual should be (expected)
      }

      it("should return true if the value is a object") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[ObjectReference]
        )

        val actual = pureValueInfoProfile.isObject

        actual should be (expected)
      }
    }

    describe("#isString") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isString

        actual should be (expected)
      }

      it("should return false if the value is not a string") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isString

        actual should be (expected)
      }

      it("should return true if the value is a string") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[StringReference]
        )

        val actual = pureValueInfoProfile.isString

        actual should be (expected)
      }
    }

    describe("#isArray") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isArray

        actual should be (expected)
      }

      it("should return false if the value is not a array") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isArray

        actual should be (expected)
      }

      it("should return true if the value is a array") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[ArrayReference]
        )

        val actual = pureValueInfoProfile.isArray

        actual should be (expected)
      }
    }

    describe("#isClassLoader") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isClassLoader

        actual should be (expected)
      }

      it("should return false if the value is not a class loader") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isClassLoader

        actual should be (expected)
      }

      it("should return true if the value is a class loader") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[ClassLoaderReference]
        )

        val actual = pureValueInfoProfile.isClassLoader

        actual should be (expected)
      }
    }

    describe("#isClassObject") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isClassObject

        actual should be (expected)
      }

      it("should return false if the value is not a class object") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isClassObject

        actual should be (expected)
      }

      it("should return true if the value is a class object") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[ClassObjectReference]
        )

        val actual = pureValueInfoProfile.isClassObject

        actual should be (expected)
      }
    }

    describe("#isThreadGroup") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isThreadGroup

        actual should be (expected)
      }

      it("should return false if the value is not a thread group") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isThreadGroup

        actual should be (expected)
      }

      it("should return true if the value is a thread group") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[ThreadGroupReference]
        )

        val actual = pureValueInfoProfile.isThreadGroup

        actual should be (expected)
      }
    }

    describe("#isThread") {
      it("should return false if the value is null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isThread

        actual should be (expected)
      }

      it("should return false if the value is not a thread") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isThread

        actual should be (expected)
      }

      it("should return true if the value is a thread") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[ThreadReference]
        )

        val actual = pureValueInfoProfile.isThread

        actual should be (expected)
      }
    }

    describe("#isNull") {
      it("should return true if the value is null") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isNull

        actual should be (expected)
      }

      it("should return false if the value is not null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isNull

        actual should be (expected)
      }
    }
  }
}
