package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureValueInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockValue = mock[Value]
  private val mockNewPrimitiveProfile = mockFunction[PrimitiveValue, PrimitiveInfoProfile]
  private val mockNewObjectProfile = mockFunction[ObjectReference, ObjectInfoProfile]
  private val mockNewArrayProfile = mockFunction[ArrayReference, ArrayInfoProfile]
  private val mockNewThreadProfile = mockFunction[ThreadReference, ThreadInfoProfile]
  private val mockNewThreadGroupProfile = mockFunction[ThreadGroupReference, ThreadGroupInfoProfile]
  private val mockNewClassObjectProfile = mockFunction[ClassObjectReference, ClassObjectInfoProfile]
  private val mockNewClassLoaderProfile = mockFunction[ClassLoaderReference, ClassLoaderInfoProfile]
  private val mockNewStringProfile = mockFunction[StringReference, StringInfoProfile]

  describe("PureValueInfoProfile") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ValueInfoProfile]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockValue
        )

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newValueInfoProfile _)
          .expects(mockScalaVirtualMachine, mockValue)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
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

        val pureValueInfoProfile = new PureValueInfoProfile(
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
        val expected = mock[TypeInfoProfile]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        ) {
          // NOTE: ScalaMock does not allow us to supply null to mock argument,
          //       so throwing an error if we aren't supplied with null
          override protected def newTypeProfile(_type: Type): TypeInfoProfile = {
            require(_type == null)
            expected
          }
        }

        val actual = pureValueInfoProfile.typeInfo

        actual should be (expected)
      }

      it("should should return a new type info profile wrapping the type") {
        val expected = mock[TypeInfoProfile]

        val mockType = mock[Type]
        (mockValue.`type` _).expects().returning(mockType).once()

        val mockNewTypeProfileFunction = mockFunction[Type, TypeInfoProfile]
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockValue
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfoProfile =
            mockNewTypeProfileFunction(_type)
        }

        mockNewTypeProfileFunction.expects(mockType).returning(expected).once()

        val actual = pureValueInfoProfile.typeInfo

        actual should be (expected)
      }
    }

    describe("#toArrayInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toArrayInfo
        }
      }

      it("should throw an assertion error if the value is not an array") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toArrayInfo
        }
      }

      it("should return an array reference wrapped in a profile") {
        val expected = mock[ArrayInfoProfile]
        val mockArrayReference = mock[ArrayReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockArrayReference
        ) {
          override protected def newArrayProfile(
            arrayReference: ArrayReference
          ): ArrayInfoProfile = mockNewArrayProfile(arrayReference)
        }

        mockNewArrayProfile.expects(mockArrayReference)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toArrayInfo

        actual should be (expected)
      }
    }

    describe("#toClassLoaderInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toClassLoaderInfo
        }
      }

      it("should throw an assertion error if the value is not an class loader") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toClassLoaderInfo
        }
      }

      it("should return an class loader reference wrapped in a profile") {
        val expected = mock[ClassLoaderInfoProfile]
        val mockClassLoaderReference = mock[ClassLoaderReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassLoaderReference
        ) {
          override protected def newClassLoaderProfile(
            classLoaderReference: ClassLoaderReference
          ): ClassLoaderInfoProfile = mockNewClassLoaderProfile(classLoaderReference)
        }

        mockNewClassLoaderProfile.expects(mockClassLoaderReference)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toClassLoaderInfo

        actual should be (expected)
      }
    }

    describe("#toClassObjectInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toClassObjectInfo
        }
      }

      it("should throw an assertion error if the value is not an class object") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toClassObjectInfo
        }
      }

      it("should return an class object reference wrapped in a profile") {
        val expected = mock[ClassObjectInfoProfile]
        val mockClassObjectReference = mock[ClassObjectReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassObjectReference
        ) {
          override protected def newClassObjectProfile(
            classObjectReference: ClassObjectReference
          ): ClassObjectInfoProfile = mockNewClassObjectProfile(classObjectReference)
        }

        mockNewClassObjectProfile.expects(mockClassObjectReference)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toClassObjectInfo

        actual should be (expected)
      }
    }

    describe("#toThreadGroupInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toThreadGroupInfo
        }
      }

      it("should throw an assertion error if the value is not an thread group") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toThreadGroupInfo
        }
      }

      it("should return an thread group reference wrapped in a profile") {
        val expected = mock[ThreadGroupInfoProfile]
        val mockThreadGroupReference = mock[ThreadGroupReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockThreadGroupReference
        ) {
          override protected def newThreadGroupProfile(
            threadGroupReference: ThreadGroupReference
          ): ThreadGroupInfoProfile = mockNewThreadGroupProfile(threadGroupReference)
        }

        mockNewThreadGroupProfile.expects(mockThreadGroupReference)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toThreadGroupInfo

        actual should be (expected)
      }
    }

    describe("#toThreadInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toThreadInfo
        }
      }

      it("should throw an assertion error if the value is not an thread") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toThreadInfo
        }
      }

      it("should return an thread reference wrapped in a profile") {
        val expected = mock[ThreadInfoProfile]
        val mockThreadReference = mock[ThreadReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockThreadReference
        ) {
          override protected def newThreadProfile(
            threadReference: ThreadReference
          ): ThreadInfoProfile = mockNewThreadProfile(threadReference)
        }

        mockNewThreadProfile.expects(mockThreadReference)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toThreadInfo

        actual should be (expected)
      }
    }

    describe("#toStringInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toStringInfo
        }
      }

      it("should throw an assertion error if the value is not a string") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toStringInfo
        }
      }

      it("should return a string reference wrapped in a profile") {
        val expected = mock[StringInfoProfile]
        val mockStringReference = mock[StringReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockStringReference
        ) {
          override protected def newStringProfile(
            stringReference: StringReference
          ): StringInfoProfile = mockNewStringProfile(stringReference)
        }

        mockNewStringProfile.expects(mockStringReference)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toStringInfo

        actual should be (expected)
      }
    }

    describe("#toPrimitiveInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toPrimitiveInfo
        }
      }

      it("should throw an assertion error if the value is not an primitive") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toPrimitiveInfo
        }
      }

      it("should return a primitive value wrapped in a profile") {
        val expected = mock[PrimitiveInfoProfile]
        val mockPrimitiveValue = mock[PrimitiveValue]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockPrimitiveValue
        ) {
          override protected def newPrimitiveProfile(
            primitiveValue: PrimitiveValue
          ): PrimitiveInfoProfile = mockNewPrimitiveProfile(primitiveValue)
        }

        mockNewPrimitiveProfile.expects(mockPrimitiveValue)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toPrimitiveInfo

        actual should be (expected)
      }

      it("should return a void value wrapped in a profile") {
        val expected = mock[PrimitiveInfoProfile]
        val mockVoidValue = mock[VoidValue]

        val mockNewPrimitiveProfile = mockFunction[VoidValue, PrimitiveInfoProfile]
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockVoidValue
        ) {
          override protected def newPrimitiveProfile(
            voidValue: VoidValue
          ): PrimitiveInfoProfile = mockNewPrimitiveProfile(voidValue)
        }

        mockNewPrimitiveProfile.expects(mockVoidValue)
          .returning(expected).once()

        val actual = pureValueInfoProfile.toPrimitiveInfo

        actual should be (expected)
      }
    }

    describe("#toObjectInfo") {
      it("should throw an assertion error if the value is null") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toObjectInfo
        }
      }

      it("should throw an assertion error if the value is not an object") {
        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        intercept[AssertionError] {
          pureValueInfoProfile.toObjectInfo
        }
      }

      it("should return an object reference wrapped in a profile") {
        val expected = mock[ObjectInfoProfile]
        val mockObjectReference = mock[ObjectReference]

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockObjectReference
        ) {
          override protected def newObjectProfile(
            arrayReference: ObjectReference
          ): ObjectInfoProfile = mockNewObjectProfile(arrayReference)
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

        val pureValueInfoProfile = new PureValueInfoProfile(
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

        val pureValueInfoProfile = new PureValueInfoProfile(
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

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return false if the value is not a primitive") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return true if the value is a primitive") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[PrimitiveValue]
        )

        val actual = pureValueInfoProfile.isPrimitive

        actual should be (expected)
      }

      it("should return true if the value is void (considered primitive)") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
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

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isVoid

        actual should be (expected)
      }

      it("should return false if the value is not void") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isVoid

        actual should be (expected)
      }

      it("should return true if the value is void") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
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

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isObject

        actual should be (expected)
      }

      it("should return false if the value is not a object") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isObject

        actual should be (expected)
      }

      it("should return true if the value is a object") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
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

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isString

        actual should be (expected)
      }

      it("should return false if the value is not a string") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isString

        actual should be (expected)
      }

      it("should return true if the value is a string") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
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

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isArray

        actual should be (expected)
      }

      it("should return false if the value is not a array") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isArray

        actual should be (expected)
      }

      it("should return true if the value is a array") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
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

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isClassLoader

        actual should be (expected)
      }

      it("should return false if the value is not a class loader") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isClassLoader

        actual should be (expected)
      }

      it("should return true if the value is a class loader") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
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

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isClassObject

        actual should be (expected)
      }

      it("should return false if the value is not a class object") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isClassObject

        actual should be (expected)
      }

      it("should return true if the value is a class object") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
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

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isThreadGroup

        actual should be (expected)
      }

      it("should return false if the value is not a thread group") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isThreadGroup

        actual should be (expected)
      }

      it("should return true if the value is a thread group") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
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

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isThread

        actual should be (expected)
      }

      it("should return false if the value is not a thread") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mock[Value]
        )

        val actual = pureValueInfoProfile.isThread

        actual should be (expected)
      }

      it("should return true if the value is a thread") {
        val expected = true

        val pureValueInfoProfile = new PureValueInfoProfile(
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

        val pureValueInfoProfile = new PureValueInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          null
        )

        val actual = pureValueInfoProfile.isNull

        actual should be (expected)
      }

      it("should return false if the value is not null") {
        val expected = false

        val pureValueInfoProfile = new PureValueInfoProfile(
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
