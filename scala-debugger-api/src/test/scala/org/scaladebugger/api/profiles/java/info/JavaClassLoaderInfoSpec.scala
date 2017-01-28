package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class JavaClassLoaderInfoSpec extends ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockReferenceType = mock[ReferenceType]
  private val mockClassLoaderReference = mock[ClassLoaderReference]
  private val javaClassLoaderInfoProfile = new JavaClassLoaderInfo(
    mockScalaVirtualMachine, mockInfoProducerProfile, mockClassLoaderReference
  )(
    _virtualMachine = mockVirtualMachine,
    _referenceType = mockReferenceType
  ) {
    override protected def newTypeProfile(_type: Type): TypeInfo =
      mockNewTypeProfile(_type)
  }

  describe("JavaClassLoaderInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ClassLoaderInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        // NOTE: Cannot validate second set of args because they are
        //       call-by-name, which ScalaMock does not support presently
        (mockInfoProducerProfile.newClassLoaderInfo(
          _: ScalaVirtualMachine,
          _: ClassLoaderReference
        )(
          _: VirtualMachine,
          _: ReferenceType
        )).expects(
          mockScalaVirtualMachine,
          mockClassLoaderReference,
          *, *
        ).returning(expected).once()

        val actual = javaClassLoaderInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaClassLoaderInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockClassLoaderReference

        val actual = javaClassLoaderInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#definedClasses") {
      it("should return a collection of profile wrappers for reference types of defined classes") {
        val expected = Seq(mock[ReferenceTypeInfo])
        val referenceTypes = Seq(mock[ReferenceType])

        import scala.collection.JavaConverters._
        (mockClassLoaderReference.definedClasses _).expects()
          .returning(referenceTypes.asJava).once()

        expected.zip(referenceTypes).foreach { case (e, r) =>
          val mockTypeInfoProfile = mock[TypeInfo]
          mockNewTypeProfile.expects(r).returning(mockTypeInfoProfile).once()
          (mockTypeInfoProfile.toReferenceType _).expects().returning(e).once()
        }

        val actual = javaClassLoaderInfoProfile.definedClasses

        actual should be (expected)
      }
    }

    describe("#visibleClasses") {
      it("should return a collection of profile wrappers for reference types of visible classes") {
        val expected = Seq(mock[ReferenceTypeInfo])
        val referenceTypes = Seq(mock[ReferenceType])

        import scala.collection.JavaConverters._
        (mockClassLoaderReference.visibleClasses _).expects()
          .returning(referenceTypes.asJava).once()

        expected.zip(referenceTypes).foreach { case (e, r) =>
          val mockTypeInfoProfile = mock[TypeInfo]
          mockNewTypeProfile.expects(r).returning(mockTypeInfoProfile).once()
          (mockTypeInfoProfile.toReferenceType _).expects().returning(e).once()
        }

        val actual = javaClassLoaderInfoProfile.visibleClasses

        actual should be (expected)
      }
    }
  }
}
