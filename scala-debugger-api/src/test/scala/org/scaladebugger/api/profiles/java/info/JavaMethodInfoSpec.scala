package org.scaladebugger.api.profiles.java.info

import com.sun.jdi.{Method, ReferenceType, Type}
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, MethodInfo, ReferenceTypeInfo, TypeInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class JavaMethodInfoSpec extends ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockMethod = mock[Method]
  private val javaMethodInfoProfile = new JavaMethodInfo(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockMethod
  ) {
    override protected def newTypeProfile(_type: Type): TypeInfo =
      mockNewTypeProfile(_type)
  }

  describe("JavaMethodInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[MethodInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newMethodInfo _)
          .expects(mockScalaVirtualMachine, mockMethod)
          .returning(expected).once()

        val actual = javaMethodInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaMethodInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockMethod

        val actual = javaMethodInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the name of the method") {
        val expected = "someName"

        (mockMethod.name _).expects().returning(expected).once()

        val actual = javaMethodInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#returnTypeInfo") {
      it("should return the type information for the return type") {
        val expected = mock[TypeInfo]

        val mockType = mock[Type]
        (mockMethod.returnType _).expects().returning(mockType).once()

        mockNewTypeProfile.expects(mockType).returning(expected).once()

        val actual = javaMethodInfoProfile.returnType

        actual should be (expected)
      }
    }

    describe("#returnTypeName") {
      it("should return the name of the method's return type") {
        val expected = "some.return.type"

        (mockMethod.returnTypeName _).expects().returning(expected).once()

        val actual = javaMethodInfoProfile.returnTypeName

        actual should be (expected)
      }
    }

    describe("#declaringTypeInfo") {
      it("should return a new type info profile wrapping the type that declared this method") {
        val expected = mock[ReferenceTypeInfo]

        val mockReferenceType = mock[ReferenceType]
        (mockMethod.declaringType _).expects()
          .returning(mockReferenceType).once()
        (mockInfoProducerProfile.newReferenceTypeInfo _)
          .expects(mockScalaVirtualMachine, mockReferenceType)
          .returning(expected)
          .once()

        val actual = javaMethodInfoProfile.declaringType

        actual should be (expected)
      }
    }

    describe("#parameterTypeInfo") {
      it("should return the type information for the parameter types") {
        val expected = Seq(mock[TypeInfo])

        import scala.collection.JavaConverters._
        val mockTypes: Seq[Type] = expected.map(_ => mock[Type])
        (mockMethod.argumentTypes _).expects()
          .returning(mockTypes.asJava).once()

        expected.zip(mockTypes).foreach { case (e, t) =>
          mockNewTypeProfile.expects(t).returning(e).once()
        }

        val actual = javaMethodInfoProfile.parameterTypes

        actual should be (expected)
      }
    }

    describe("#parameterTypeNames") {
      it("should return an ordered collection of parameter names for the method") {
        val expected = Seq("some.parameter.type", "some.other.parameter.type")

        import scala.collection.JavaConverters._
        (mockMethod.argumentTypeNames _).expects()
          .returning(expected.asJava).once()

        val actual = javaMethodInfoProfile.parameterTypeNames

        actual should be (expected)
      }
    }
  }
}
