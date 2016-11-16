package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.{InvokeSingleThreadedArgument, JDIArgument}
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureClassTypeInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfoProfile]
  private val mockNewObjectProfile = mockFunction[ObjectReference, VirtualMachine, ObjectInfoProfile]
  private val mockNewInterfaceTypeProfile = mockFunction[InterfaceType, InterfaceTypeInfoProfile]
  private val mockNewClassTypeProfile = mockFunction[ClassType, ClassTypeInfoProfile]
  private val mockNewMethodProfile = mockFunction[Method, MethodInfoProfile]
  private val mockNewValueProfile = mockFunction[Value, ValueInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockClassType = mock[ClassType]
  private val pureClassTypeInfoProfile = new PureClassTypeInfoProfile(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockClassType
  ) {
    override protected def newTypeProfile(_type: Type): TypeInfoProfile =
      mockNewTypeProfile(_type)

    override protected def newObjectProfile(
      objectReference: ObjectReference,
      virtualMachine: VirtualMachine
    ): ObjectInfoProfile = mockNewObjectProfile(
      objectReference,
      virtualMachine
    )

    override protected def newInterfaceTypeProfile(
      interfaceType: InterfaceType
    ): InterfaceTypeInfoProfile = mockNewInterfaceTypeProfile(interfaceType)


    override protected def newClassTypeProfile(
      classType: ClassType
    ): ClassTypeInfoProfile = mockNewClassTypeProfile(classType)


    override protected def newMethodProfile(method: Method): MethodInfoProfile =
      mockNewMethodProfile(method)

    override protected def newValueProfile(value: Value): ValueInfoProfile =
      mockNewValueProfile(value)
  }

  describe("PureClassTypeInfoProfile") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ClassTypeInfoProfile]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newClassTypeInfoProfile _)
          .expects(mockScalaVirtualMachine, mockClassType)
          .returning(expected).once()

        val actual = pureClassTypeInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureClassTypeInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockClassType

        val actual = pureClassTypeInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#allInterfaces") {
      it("should return all interfaces directly and indirectly implemented by this class") {
        val expected = Seq(mock[InterfaceTypeInfoProfile])
        val mockInterfaces = expected.map(_ => mock[InterfaceType])

        import scala.collection.JavaConverters._
        (mockClassType.allInterfaces _).expects()
          .returning(mockInterfaces.asJava).once()

        expected.zip(mockInterfaces).foreach { case (e, i) =>
          mockNewInterfaceTypeProfile.expects(i).returning(e).once()
        }

        val actual = pureClassTypeInfoProfile.allInterfaces

        actual should be (expected)
      }
    }

    describe("#interfaces") {
      it("should return all interfaces directly implemented by this class") {
        val expected = Seq(mock[InterfaceTypeInfoProfile])
        val mockInterfaces = expected.map(_ => mock[InterfaceType])

        import scala.collection.JavaConverters._
        (mockClassType.interfaces _).expects()
          .returning(mockInterfaces.asJava).once()

        expected.zip(mockInterfaces).foreach { case (e, i) =>
          mockNewInterfaceTypeProfile.expects(i).returning(e).once()
        }

        val actual = pureClassTypeInfoProfile.interfaces

        actual should be (expected)
      }
    }

    describe("#superclassOption") {
      it("should return Some(ClassType) if this class has a superclass") {
        val expected = Some(mock[ClassTypeInfoProfile])

        (mockClassType.superclass _).expects().returning(mockClassType).once()

        mockNewClassTypeProfile.expects(mockClassType)
          .returning(expected.get).once()

        val actual = pureClassTypeInfoProfile.superclassOption

        actual should be (expected)
      }

      it("should return None if this class has no superclass") {
        val expected = None

        (mockClassType.superclass _).expects().returning(null).once()

        val actual = pureClassTypeInfoProfile.superclassOption

        actual should be (expected)
      }
    }

    describe("#subclasses") {
      it("should return all classes directly inheriting from this class") {
        val expected = Seq(mock[ClassTypeInfoProfile])
        val mockSubclasses = expected.map(_ => mock[ClassType])

        import scala.collection.JavaConverters._
        (mockClassType.subclasses _).expects()
          .returning(mockSubclasses.asJava).once()

        expected.zip(mockSubclasses).foreach { case (e, i) =>
          mockNewClassTypeProfile.expects(i).returning(e).once()
        }

        val actual = pureClassTypeInfoProfile.subclasses

        actual should be (expected)
      }
    }

    describe("#isEnumeration") {
      it("should return true if the class represents a Java enumeration") {
        val expected = true

        (mockClassType.isEnum _).expects().returning(expected).once()

        val actual = pureClassTypeInfoProfile.isEnumeration

        actual should be (expected)
      }

      it("should return false if the class does not represent a Java enumeration") {
        val expected = false

        (mockClassType.isEnum _).expects().returning(expected).once()

        val actual = pureClassTypeInfoProfile.isEnumeration

        actual should be (expected)
      }
    }

    describe("#method") {
      it("should return Some(MethodInfo) if a matching method is found") {
        val expected = Some(mock[MethodInfoProfile])
        val methodName = "someMethod"
        val methodSignature = "signature"

        val mockMethod = mock[Method]
        (mockClassType.concreteMethodByName _)
          .expects(methodName, methodSignature)
          .returning(mockMethod)
          .once()

        mockNewMethodProfile.expects(mockMethod)
          .returning(expected.get).once()

        val actual = pureClassTypeInfoProfile.methodOption(methodName, methodSignature)

        actual should be (expected)
      }

      it("should return None if no matching method is found") {
        val expected = None
        val methodName = "someMethod"
        val methodSignature = "signature"

        val mockMethod = mock[Method]
        (mockClassType.concreteMethodByName _)
          .expects(methodName, methodSignature)
          .returning(null)
          .once()

        val actual = pureClassTypeInfoProfile.methodOption(methodName, methodSignature)

        actual should be (expected)
      }
    }

    describe("#invoke(thread, method name, method signature, arguments, JDI arguments)") {
      it("should use method(...) to search for method with name and signature") {
        val expected = mock[ValueInfoProfile]

        val name = "methodName"
        val signature = "signature"
        val arguments = Seq(3)
        val jdiArguments = Nil

        val mockUnsafeInvoke = mockFunction[
          ThreadInfoProfile,
          MethodInfoProfile,
          Seq[Any],
          Seq[JDIArgument],
          ValueInfoProfile
        ]
        val mockUnsafeMethod = mockFunction[String, String, Option[MethodInfoProfile]]

        val pureClassTypeInfoProfile = new PureClassTypeInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassType
        ) {
          override def invokeStaticMethod(
            thread: ThreadInfoProfile,
            method: MethodInfoProfile,
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfoProfile = mockUnsafeInvoke(
            thread,
            method,
            arguments,
            jdiArguments
          )

          override def methodOption(
            name: String,
            signature: String
          ): Option[MethodInfoProfile] = mockUnsafeMethod(name, signature)
        }

        val mockThread = mock[ThreadInfoProfile]

        val mockMethod = mock[MethodInfoProfile]
        mockUnsafeMethod.expects(name, signature)
          .returning(Some(mockMethod)).once()

        mockUnsafeInvoke.expects(mockThread, mockMethod, arguments, jdiArguments)
          .returning(expected).once()

        val actual = pureClassTypeInfoProfile.invokeStaticMethod(
          mockThread,
          name,
          signature,
          arguments,
          jdiArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#invokeStaticMethod(thread, method, arguments, JDI arguments)") {
      it("should invoke using the provided thread and method, returning wrapper profile of value") {
        val expected = mock[ValueInfoProfile]
        val mockThread = mock[ThreadReference]
        val mockThreadInfo = mock[ThreadInfoProfile]
        (mockThreadInfo.toJdiInstance _).expects().returning(mockThread).once()

        val mockMethod = mock[Method]
        val methodInfo = new PureMethodInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockMethod
        )

        // Static class method is invoked
        val mockValue = mock[Value]
        import scala.collection.JavaConverters._
        (mockClassType.invokeMethod _).expects(
          mockThread,
          mockMethod,
          Seq[Value]().asJava,
          0
        ).returning(mockValue).once()

        // Profile is created for return value
        mockNewValueProfile.expects(mockValue).returning(expected).once()

        val actual = pureClassTypeInfoProfile.invokeStaticMethod(
          mockThreadInfo,
          methodInfo,
          Nil
        )

        actual should be (expected)
      }

      it("should invoke using the provided arguments") {
        val arguments = Seq(1)
        val mockThread = mock[ThreadReference]
        val mockThreadInfo = mock[ThreadInfoProfile]
        (mockThreadInfo.toJdiInstance _).expects().returning(mockThread).once()

        val mockMethod = mock[Method]
        val methodInfo = new PureMethodInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockMethod
        )

        val mockVirtualMachine = mock[VirtualMachine]
        val pureClassTypeInfoProfile = new PureClassTypeInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassType
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfoProfile =
            mockNewTypeProfile(_type)

          override protected def newValueProfile(value: Value): ValueInfoProfile =
            mockNewValueProfile(value)

          override protected def newVirtualMachine(): VirtualMachine =
            mockVirtualMachine
        }

        // Arguments are mirrored remotely
        val mockValues = Seq(mock[IntegerValue])
        arguments.zip(mockValues).foreach { case (ar, ma) =>
          (mockVirtualMachine.mirrorOf(_: Int)).expects(ar)
            .returning(ma).once()
        }

        // Object method is invoked
        val mockValue = mock[Value]
        import scala.collection.JavaConverters._
        (mockClassType.invokeMethod _).expects(
          *,
          *,
          mockValues.asJava,
          *
        ).returning(mockValue).once()

        // Profile is created for return value
        mockNewValueProfile.expects(*).once()

        pureClassTypeInfoProfile.invokeStaticMethod(
          mockThreadInfo,
          methodInfo,
          arguments
        )
      }

      it("should provide relevant JDI options as an OR'd value") {
        val jdiArguments = Seq(
          InvokeSingleThreadedArgument
        )
        val mockThread = mock[ThreadReference]
        val mockThreadInfo = mock[ThreadInfoProfile]
        (mockThreadInfo.toJdiInstance _).expects().returning(mockThread).once()

        val mockMethod = mock[Method]
        val methodInfo = new PureMethodInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockMethod
        )

        // Object method is invoked (given flag with value 1)
        (mockClassType.invokeMethod _).expects(*, *, *, 1)
          .returning(mock[Value]).once()

        // Profile is created for return value
        mockNewValueProfile.expects(*).returning(null).once()

        pureClassTypeInfoProfile.invokeStaticMethod(
          mockThreadInfo,
          methodInfo,
          Nil,
          jdiArguments: _*
        )
      }
    }

    describe("#newInstance(thread, constructor, arguments, JDI arguments)") {
      it("should use method(...) to search for constructor with name and signature") {
        val expected = mock[ValueInfoProfile]

        val name = "methodName"
        val signature = "signature"
        val arguments = Seq(3)
        val jdiArguments = Nil

        val mockUnsafeInvoke = mockFunction[
          ThreadInfoProfile,
          MethodInfoProfile,
          Seq[Any],
          Seq[JDIArgument],
          ValueInfoProfile
        ]
        val mockUnsafeMethod = mockFunction[String, String, Option[MethodInfoProfile]]

        val pureClassTypeInfoProfile = new PureClassTypeInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassType
        ) {
          override def invokeStaticMethod(
            thread: ThreadInfoProfile,
            method: MethodInfoProfile,
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfoProfile = mockUnsafeInvoke(
            thread,
            method,
            arguments,
            jdiArguments
          )

          override def methodOption(
            name: String,
            signature: String
          ): Option[MethodInfoProfile] = mockUnsafeMethod(name, signature)
        }

        val mockThread = mock[ThreadInfoProfile]

        val mockMethod = mock[MethodInfoProfile]
        mockUnsafeMethod.expects(name, signature)
          .returning(Some(mockMethod)).once()

        mockUnsafeInvoke.expects(mockThread, mockMethod, arguments, jdiArguments)
          .returning(expected).once()

        val actual = pureClassTypeInfoProfile.invokeStaticMethod(
          mockThread,
          name,
          signature,
          arguments,
          jdiArguments: _*
        )

        actual should be (expected)
      }    }

    describe("#newInstance(thread, constructor name, constructor signature, arguments, JDI arguments)") {
      it("should invoke using the provided thread and constructor, returning wrapper profile of object") {
        val expected = mock[ObjectInfoProfile]
        val mockThread = mock[ThreadReference]
        val mockThreadInfo = mock[ThreadInfoProfile]
        (mockThreadInfo.toJdiInstance _).expects().returning(mockThread).once()

        val mockMethod = mock[Method]
        val methodInfo = new PureMethodInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockMethod
        )

        val mockVirtualMachine = mock[VirtualMachine]
        val pureClassTypeInfoProfile = new PureClassTypeInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassType
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfoProfile =
            mockNewTypeProfile(_type)

          override protected def newValueProfile(value: Value): ValueInfoProfile =
            mockNewValueProfile(value)

          override protected def newVirtualMachine(): VirtualMachine =
            mockVirtualMachine

          override protected def newObjectProfile(
            objectReference: ObjectReference,
            virtualMachine: VirtualMachine
          ): ObjectInfoProfile = mockNewObjectProfile(
            objectReference,
            virtualMachine
          )
        }

        // Static class method is invoked
        val mockObjectReference = mock[ObjectReference]
        import scala.collection.JavaConverters._
        (mockClassType.newInstance _).expects(
          mockThread,
          mockMethod,
          Seq[Value]().asJava,
          0
        ).returning(mockObjectReference).once()

        // Profile is created for return value
        mockNewObjectProfile.expects(
          mockObjectReference,
          mockVirtualMachine
        ).returning(expected).once()

        val actual = pureClassTypeInfoProfile.newInstance(
          mockThreadInfo,
          methodInfo,
          Nil
        )

        actual should be (expected)
      }

      it("should invoke using the provided arguments") {
        val arguments = Seq(1)
        val mockThread = mock[ThreadReference]
        val mockThreadInfo = mock[ThreadInfoProfile]
        (mockThreadInfo.toJdiInstance _).expects().returning(mockThread).once()

        val mockMethod = mock[Method]
        val methodInfo = new PureMethodInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockMethod
        )

        val mockVirtualMachine = mock[VirtualMachine]
        val pureClassTypeInfoProfile = new PureClassTypeInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassType
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfoProfile =
            mockNewTypeProfile(_type)

          override protected def newValueProfile(value: Value): ValueInfoProfile =
            mockNewValueProfile(value)

          override protected def newVirtualMachine(): VirtualMachine =
            mockVirtualMachine

          override protected def newObjectProfile(
            objectReference: ObjectReference,
            virtualMachine: VirtualMachine
          ): ObjectInfoProfile = mockNewObjectProfile(
            objectReference,
            virtualMachine
          )
        }

        // Arguments are mirrored remotely
        val mockValues = Seq(mock[IntegerValue])
        arguments.zip(mockValues).foreach { case (ar, ma) =>
          (mockVirtualMachine.mirrorOf(_: Int)).expects(ar)
            .returning(ma).once()
        }

        // Static class method is invoked
        val mockObjectReference = mock[ObjectReference]
        import scala.collection.JavaConverters._
        (mockClassType.newInstance _).expects(
          *,
          *,
          mockValues.asJava,
          *
        ).returning(mockObjectReference).once()

        // Profile is created for return value
        mockNewObjectProfile.expects(*, *).once()

        pureClassTypeInfoProfile.newInstance(
          mockThreadInfo,
          methodInfo,
          arguments
        )
      }

      it("should provide relevant JDI options as an OR'd value") {
        val jdiArguments = Seq(
          InvokeSingleThreadedArgument
        )
        val mockThread = mock[ThreadReference]
        val mockThreadInfo = mock[ThreadInfoProfile]
        (mockThreadInfo.toJdiInstance _).expects().returning(mockThread).once()

        val mockMethod = mock[Method]
        val methodInfo = new PureMethodInfoProfile(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockMethod
        )

        // Object method is invoked (given flag with value 1)
        val mockObjectReference = mock[ObjectReference]
        (mockClassType.newInstance _).expects(*, *, *, 1)
          .returning(mockObjectReference).once()

        // Profile is created for return value
        mockNewObjectProfile.expects(*, *).returning(null).once()

        pureClassTypeInfoProfile.newInstance(
          mockThreadInfo,
          methodInfo,
          Nil,
          jdiArguments: _*
        )
      }
    }
  }
}
