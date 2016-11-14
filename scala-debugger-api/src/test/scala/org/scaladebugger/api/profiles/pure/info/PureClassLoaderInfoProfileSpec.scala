package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureClassLoaderInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockReferenceType = mock[ReferenceType]
  private val mockClassLoaderReference = mock[ClassLoaderReference]
  private val pureClassLoaderInfoProfile = new PureClassLoaderInfoProfile(
    mockScalaVirtualMachine, mockInfoProducerProfile, mockClassLoaderReference
  )(
    _virtualMachine = mockVirtualMachine,
    _referenceType = mockReferenceType
  ) {
    override protected def newTypeProfile(_type: Type): TypeInfoProfile =
      mockNewTypeProfile(_type)
  }

  describe("PureClassLoaderInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockClassLoaderReference

        val actual = pureClassLoaderInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#definedClasses") {
      it("should return a collection of profile wrappers for reference types of defined classes") {
        val expected = Seq(mock[ReferenceTypeInfoProfile])
        val referenceTypes = Seq(mock[ReferenceType])

        import scala.collection.JavaConverters._
        (mockClassLoaderReference.definedClasses _).expects()
          .returning(referenceTypes.asJava).once()

        expected.zip(referenceTypes).foreach { case (e, r) =>
          val mockTypeInfoProfile = mock[TypeInfoProfile]
          mockNewTypeProfile.expects(r).returning(mockTypeInfoProfile).once()
          (mockTypeInfoProfile.toReferenceType _).expects().returning(e).once()
        }

        val actual = pureClassLoaderInfoProfile.definedClasses

        actual should be (expected)
      }
    }

    describe("#visibleClasses") {
      it("should return a collection of profile wrappers for reference types of visible classes") {
        val expected = Seq(mock[ReferenceTypeInfoProfile])
        val referenceTypes = Seq(mock[ReferenceType])

        import scala.collection.JavaConverters._
        (mockClassLoaderReference.visibleClasses _).expects()
          .returning(referenceTypes.asJava).once()

        expected.zip(referenceTypes).foreach { case (e, r) =>
          val mockTypeInfoProfile = mock[TypeInfoProfile]
          mockNewTypeProfile.expects(r).returning(mockTypeInfoProfile).once()
          (mockTypeInfoProfile.toReferenceType _).expects().returning(e).once()
        }

        val actual = pureClassLoaderInfoProfile.visibleClasses

        actual should be (expected)
      }
    }
  }
}
