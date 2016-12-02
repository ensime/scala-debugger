package org.scaladebugger.api.profiles.pure.info

import java.util

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.{TestCreateInfoProfileTrait, TestMiscInfoProfileTrait}

class PureArrayInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockReferenceType = mock[ReferenceType]
  private val mockArrayReference = mock[ArrayReference]
  private val pureArrayInfoProfile = new PureArrayInfoProfile(
    mockScalaVirtualMachine, mockInfoProducerProfile, mockArrayReference
  )(
    _virtualMachine = mockVirtualMachine,
    _referenceType = mockReferenceType
  ) {
    override protected def newTypeProfile(_type: Type): TypeInfoProfile =
      mockNewTypeProfile(_type)
  }

  describe("PureArrayInfoProfile") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ArrayInfoProfile]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducerProfile.newArrayInfoProfile(
          _: ScalaVirtualMachine,
          _: ArrayReference
        )(
          _: VirtualMachine,
          _: ReferenceType
        )).expects(
          mockScalaVirtualMachine,
          mockArrayReference,
          *, *
        ).returning(expected).once()

        val actual = pureArrayInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureArrayInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockArrayReference

        val actual = pureArrayInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#typeInfo") {
      it("should should return a new array type profile wrapping the type") {
        val expected = mock[ArrayTypeInfoProfile]

        val mockArrayType = mock[ArrayType]
        (mockArrayReference.`type` _).expects().returning(mockArrayType).once()

        val mockTypeInfoProfile = mock[TypeInfoProfile]
        mockNewTypeProfile.expects(mockArrayType)
          .returning(mockTypeInfoProfile).once()

        val mockReferenceTypeInfoProfile = mock[ReferenceTypeInfoProfile]
        (mockTypeInfoProfile.toReferenceType _).expects()
          .returning(mockReferenceTypeInfoProfile).once()

        (mockReferenceTypeInfoProfile.toArrayType _).expects()
          .returning(expected).once()

        val actual = pureArrayInfoProfile.typeInfo

        actual should be (expected)
      }
    }

    describe("#length") {
      it("should return the length provided by the array reference") {
        val expected = 999

        (mockArrayReference.length _).expects().returning(expected).once()

        val actual = pureArrayInfoProfile.length

        actual should be (expected)
      }
    }

    describe("#value") {
      it("should return a new value profile wrapping the value at the position") {
        val expected = mock[ValueInfoProfile]
        val mockValue = mock[Value]

        val pureArrayInfoProfile = new PureArrayInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockArrayReference
        )(
          _virtualMachine = mockVirtualMachine,
          _referenceType = mockReferenceType
        ) {
          override protected def newValueProfile(value: Value): ValueInfoProfile = {
            value should be (mockValue)
            expected
          }
        }

        (mockArrayReference.getValue _).expects(0).returning(mockValue).once()
        val actual = pureArrayInfoProfile.value(0)

        actual should be (expected)
      }
    }

    describe("#values(index, totalElements)") {
      it("should return value profiles wrapping the return values") {
        val expected = Seq(mock[ValueInfoProfile])
        val mockValues = Seq(mock[Value])

        val pureArrayInfoProfile = new PureArrayInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockArrayReference
        )(
          _virtualMachine = mockVirtualMachine,
          _referenceType = mockReferenceType
        ) {
          override protected def newValueProfile(value: Value): ValueInfoProfile = {
            value should be (mockValues.head)
            expected.head
          }
        }

        import scala.collection.JavaConverters._
        (mockArrayReference.getValues(_: Int, _: Int)).expects(0, 0)
          .returning(mockValues.asJava).once()
        val actual = pureArrayInfoProfile.values(0, 0)

        actual should be (expected)
      }
    }

    describe("#values") {
      it("should return value profiles wrapping the return values") {
        val expected = Seq(mock[ValueInfoProfile])
        val mockValues = Seq(mock[Value])

        val pureArrayInfoProfile = new PureArrayInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, mockArrayReference
        )(
          _virtualMachine = mockVirtualMachine,
          _referenceType = mockReferenceType
        ) {
          override protected def newValueProfile(value: Value): ValueInfoProfile = {
            value should be (mockValues.head)
            expected.head
          }
        }

        import scala.collection.JavaConverters._
        (mockArrayReference.getValues: Function0[java.util.List[Value]]).expects()
          .returning(mockValues.asJava).once()
        val actual = pureArrayInfoProfile.values

        actual should be (expected)
      }
    }

    describe("#setValueFromInfo") {
      it("should set and return the provided value at the specified position") {
        val expected = mock[ValueInfoProfile]
        val index = 999
        val mockValue = mock[StringReference]

        (expected.toJdiInstance _).expects().returning(mockValue).once()
        (mockArrayReference.setValue _).expects(index, mockValue).once()

        val actual = pureArrayInfoProfile.setValueFromInfo(index, expected)

        actual should be (expected)
      }
    }

    describe("#setValuesFromInfo(index, values, srcIndex, totalElements)") {
      it("should set and return the values starting from the src index to total elements") {
        val expected = Seq(mock[ValueInfoProfile], mock[ValueInfoProfile])

        val index = 999
        val values = mock[ValueInfoProfile] +: expected
        val srcIndex = 1
        val totalElements = 2

        val mockValues = values.map(_ => mock[PrimitiveValue])

        val mockSetValues = mockFunction[Int, java.util.List[_ <: Value], Int, Int, Unit]
        val testArrayReference = new TestArrayReference {
          override def setValues(i: Int, list: util.List[_ <: Value], i1: Int, i2: Int): Unit =
            mockSetValues(i, list, i1, i2)
        }

        values.zip(mockValues).foreach { case (e, v) =>
          (e.toJdiInstance _).expects().returning(v).once()
        }

        import scala.collection.JavaConverters._
        mockSetValues.expects(index, mockValues.asJava, srcIndex, totalElements).once()
        val pureArrayInfoProfile = new PureArrayInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, testArrayReference
        )(
          _virtualMachine = mockVirtualMachine,
          _referenceType = mockReferenceType
        )

        val actual = pureArrayInfoProfile.setValuesFromInfo(
          index,
          values,
          srcIndex,
          totalElements
        )

        actual should be (expected)
      }
    }

    describe("#setValuesFromInfo(values)") {
      it("should set and return the provided values") {
        val expected = Seq(mock[ValueInfoProfile], mock[ValueInfoProfile])

        val mockValues = expected.map(_ => mock[PrimitiveValue])
        val mockSetValues = mockFunction[java.util.List[_ <: Value], Unit]
        val testArrayReference = new TestArrayReference {
          override def setValues(list: util.List[_ <: Value]): Unit = mockSetValues(list)
        }

        expected.zip(mockValues).foreach { case (e, v) =>
          (e.toJdiInstance _).expects().returning(v).once()
        }

        import scala.collection.JavaConverters._
        mockSetValues.expects(mockValues.asJava).once()
        val pureArrayInfoProfile = new PureArrayInfoProfile(
          mockScalaVirtualMachine, mockInfoProducerProfile, testArrayReference
        )(
          _virtualMachine = mockVirtualMachine,
          _referenceType = mockReferenceType
        )

        val actual = pureArrayInfoProfile.setValuesFromInfo(expected)

        actual should be (expected)
      }
    }
  }

  // NOTE: This class exists so we can inject mock methods as ScalaMock has
  //       a bug where it cannot mock methods with generics
  //
  //       See https://github.com/paulbutcher/ScalaMock/issues/94
  private class TestArrayReference extends ArrayReference {
    override def getValue(i: Int): Value = ???
    override def setValue(i: Int, value: Value): Unit = ???
    override def length(): Int = ???
    override def setValues(list: util.List[_ <: Value]): Unit = ???
    override def setValues(i: Int, list: util.List[_ <: Value], i1: Int, i2: Int): Unit = ???
    override def getValues: util.List[Value] = ???
    override def getValues(i: Int, i1: Int): util.List[Value] = ???
    override def getValue(field: Field): Value = ???
    override def setValue(field: Field, value: Value): Unit = ???
    override def owningThread(): ThreadReference = ???
    override def referringObjects(l: Long): util.List[ObjectReference] = ???
    override def referenceType(): ReferenceType = ???
    override def uniqueID(): Long = ???
    override def invokeMethod(threadReference: ThreadReference, method: Method, list: util.List[_ <: Value], i: Int): Value = ???
    override def disableCollection(): Unit = ???
    override def enableCollection(): Unit = ???
    override def isCollected: Boolean = ???
    override def getValues(list: util.List[_ <: Field]): util.Map[Field, Value] = ???
    override def waitingThreads(): util.List[ThreadReference] = ???
    override def entryCount(): Int = ???
    override def `type`(): Type = ???
    override def virtualMachine(): VirtualMachine = ???
  }
}
