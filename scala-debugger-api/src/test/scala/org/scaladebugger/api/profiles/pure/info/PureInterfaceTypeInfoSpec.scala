package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ClassTypeInfo, InfoProducer, InterfaceTypeInfo, TypeInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureInterfaceTypeInfoSpec extends ParallelMockFunSpec
{
  private val mockNewInterfaceTypeProfile = mockFunction[InterfaceType, InterfaceTypeInfo]
  private val mockNewClassTypeProfile = mockFunction[ClassType, ClassTypeInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockInterfaceType = mock[InterfaceType]
  private val pureInterfaceTypeInfoProfile = new PureInterfaceTypeInfo(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockInterfaceType
  ) {
    override protected def newInterfaceTypeProfile(
      interfaceType: InterfaceType
    ): InterfaceTypeInfo = mockNewInterfaceTypeProfile(interfaceType)

    override protected def newClassTypeProfile(
      classType: ClassType
    ): ClassTypeInfo = mockNewClassTypeProfile(classType)
  }

  describe("PureInterfaceTypeInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[InterfaceTypeInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newInterfaceTypeInfo _)
          .expects(mockScalaVirtualMachine, mockInterfaceType)
          .returning(expected).once()

        val actual = pureInterfaceTypeInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureInterfaceTypeInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockInterfaceType

        val actual = pureInterfaceTypeInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#implementors") {
      it("should return the implementors of the interface") {
        val expected = Seq(mock[ClassTypeInfo])

        import scala.collection.JavaConverters._
        val mockImplementors = expected.map(_ => mock[ClassType])
        (mockInterfaceType.implementors _).expects()
          .returning(mockImplementors.asJava).once()

        expected.zip(mockImplementors).foreach { case (e, i) =>
          mockNewClassTypeProfile.expects(i).returning(e).once()
        }

        val actual = pureInterfaceTypeInfoProfile.implementors

        actual should be (expected)
      }
    }

    describe("#subinterfaces") {
      it("should return the subinterfaces of the interface") {
        val expected = Seq(mock[InterfaceTypeInfo])

        import scala.collection.JavaConverters._
        val mockSubinterfaces = expected.map(_ => mock[InterfaceType])
        (mockInterfaceType.subinterfaces _).expects()
          .returning(mockSubinterfaces.asJava).once()

        expected.zip(mockSubinterfaces).foreach { case (e, s) =>
          mockNewInterfaceTypeProfile.expects(s).returning(e).once()
        }

        val actual = pureInterfaceTypeInfoProfile.subinterfaces

        actual should be (expected)
      }
    }

    describe("#superinterfaces") {
      it("should return the superinterfaces of the interface") {
        val expected = Seq(mock[InterfaceTypeInfo])

        import scala.collection.JavaConverters._
        val mockSuperinterfaces = expected.map(_ => mock[InterfaceType])
        (mockInterfaceType.superinterfaces _).expects()
          .returning(mockSuperinterfaces.asJava).once()

        expected.zip(mockSuperinterfaces).foreach { case (e, s) =>
          mockNewInterfaceTypeProfile.expects(s).returning(e).once()
        }

        val actual = pureInterfaceTypeInfoProfile.superinterfaces

        actual should be (expected)
      }
    }
  }
}
