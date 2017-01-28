package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.{InvokeSingleThreadedArgument, JDIArgument}
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class JavaClassTypeInfoSpec extends ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfo]
  private val mockNewObjectProfile = mockFunction[ObjectReference, VirtualMachine, ObjectInfo]
  private val mockNewInterfaceTypeProfile = mockFunction[InterfaceType, InterfaceTypeInfo]
  private val mockNewClassTypeProfile = mockFunction[ClassType, ClassTypeInfo]
  private val mockNewMethodProfile = mockFunction[Method, MethodInfo]
  private val mockNewValueProfile = mockFunction[Value, ValueInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockClassType = mock[ClassType]
  private val javaClassTypeInfoProfile = new JavaClassTypeInfo(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockClassType
  ) {
    override protected def newTypeProfile(_type: Type): TypeInfo =
      mockNewTypeProfile(_type)

    override protected def newObjectProfile(
      objectReference: ObjectReference,
      virtualMachine: VirtualMachine
    ): ObjectInfo = mockNewObjectProfile(
      objectReference,
      virtualMachine
    )

    override protected def newInterfaceTypeProfile(
      interfaceType: InterfaceType
    ): InterfaceTypeInfo = mockNewInterfaceTypeProfile(interfaceType)


    override protected def newClassTypeProfile(
      classType: ClassType
    ): ClassTypeInfo = mockNewClassTypeProfile(classType)


    override protected def newMethodProfile(method: Method): MethodInfo =
      mockNewMethodProfile(method)

    override protected def newValueProfile(value: Value): ValueInfo =
      mockNewValueProfile(value)
  }

  describe("JavaClassTypeInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ClassTypeInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newClassTypeInfo _)
          .expects(mockScalaVirtualMachine, mockClassType)
          .returning(expected).once()

        val actual = javaClassTypeInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaClassTypeInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockClassType

        val actual = javaClassTypeInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#allInterfaces") {
      it("should return all interfaces directly and indirectly implemented by this class") {
        val expected = Seq(mock[InterfaceTypeInfo])
        val mockInterfaces = expected.map(_ => mock[InterfaceType])

        import scala.collection.JavaConverters._
        (mockClassType.allInterfaces _).expects()
          .returning(mockInterfaces.asJava).once()

        expected.zip(mockInterfaces).foreach { case (e, i) =>
          mockNewInterfaceTypeProfile.expects(i).returning(e).once()
        }

        val actual = javaClassTypeInfoProfile.allInterfaces

        actual should be (expected)
      }
    }

    describe("#interfaces") {
      it("should return all interfaces directly implemented by this class") {
        val expected = Seq(mock[InterfaceTypeInfo])
        val mockInterfaces = expected.map(_ => mock[InterfaceType])

        import scala.collection.JavaConverters._
        (mockClassType.interfaces _).expects()
          .returning(mockInterfaces.asJava).once()

        expected.zip(mockInterfaces).foreach { case (e, i) =>
          mockNewInterfaceTypeProfile.expects(i).returning(e).once()
        }

        val actual = javaClassTypeInfoProfile.interfaces

        actual should be (expected)
      }
    }

    describe("#superclassOption") {
      it("should return Some(ClassType) if this class has a superclass") {
        val expected = Some(mock[ClassTypeInfo])

        (mockClassType.superclass _).expects().returning(mockClassType).once()

        mockNewClassTypeProfile.expects(mockClassType)
          .returning(expected.get).once()

        val actual = javaClassTypeInfoProfile.superclassOption

        actual should be (expected)
      }

      it("should return None if this class has no superclass") {
        val expected = None

        (mockClassType.superclass _).expects().returning(null).once()

        val actual = javaClassTypeInfoProfile.superclassOption

        actual should be (expected)
      }
    }

    describe("#subclasses") {
      it("should return all classes directly inheriting from this class") {
        val expected = Seq(mock[ClassTypeInfo])
        val mockSubclasses = expected.map(_ => mock[ClassType])

        import scala.collection.JavaConverters._
        (mockClassType.subclasses _).expects()
          .returning(mockSubclasses.asJava).once()

        expected.zip(mockSubclasses).foreach { case (e, i) =>
          mockNewClassTypeProfile.expects(i).returning(e).once()
        }

        val actual = javaClassTypeInfoProfile.subclasses

        actual should be (expected)
      }
    }

    describe("#isEnumeration") {
      it("should return true if the class represents a Java enumeration") {
        val expected = true

        (mockClassType.isEnum _).expects().returning(expected).once()

        val actual = javaClassTypeInfoProfile.isEnumeration

        actual should be (expected)
      }

      it("should return false if the class does not represent a Java enumeration") {
        val expected = false

        (mockClassType.isEnum _).expects().returning(expected).once()

        val actual = javaClassTypeInfoProfile.isEnumeration

        actual should be (expected)
      }
    }

    describe("#method") {
      it("should return Some(MethodInfo) if a matching method is found") {
        val expected = Some(mock[MethodInfo])
        val methodName = "someMethod"
        val methodSignature = "signature"

        val mockMethod = mock[Method]
        (mockClassType.concreteMethodByName _)
          .expects(methodName, methodSignature)
          .returning(mockMethod)
          .once()

        mockNewMethodProfile.expects(mockMethod)
          .returning(expected.get).once()

        val actual = javaClassTypeInfoProfile.methodOption(methodName, methodSignature)

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

        val actual = javaClassTypeInfoProfile.methodOption(methodName, methodSignature)

        actual should be (expected)
      }
    }

    describe("#invoke(thread, method name, method signature, arguments, JDI arguments)") {
      it("should use method(...) to search for method with name and signature") {
        val expected = mock[ValueInfo]

        val name = "methodName"
        val signature = "signature"
        val arguments = Seq(3)
        val jdiArguments = Nil

        val mockUnsafeInvoke = mockFunction[
          ThreadInfo,
          MethodInfo,
          Seq[Any],
          Seq[JDIArgument],
          ValueInfo
        ]
        val mockUnsafeMethod = mockFunction[String, String, Option[MethodInfo]]

        val javaClassTypeInfoProfile = new JavaClassTypeInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassType
        ) {
          override def invokeStaticMethod(
            thread: ThreadInfo,
            method: MethodInfo,
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfo = mockUnsafeInvoke(
            thread,
            method,
            arguments,
            jdiArguments
          )

          override def methodOption(
            name: String,
            signature: String
          ): Option[MethodInfo] = mockUnsafeMethod(name, signature)
        }

        val mockThread = mock[ThreadInfo]

        val mockMethod = mock[MethodInfo]
        mockUnsafeMethod.expects(name, signature)
          .returning(Some(mockMethod)).once()

        mockUnsafeInvoke.expects(mockThread, mockMethod, arguments, jdiArguments)
          .returning(expected).once()

        val actual = javaClassTypeInfoProfile.invokeStaticMethod(
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
        val expected = mock[ValueInfo]
        val mockThread = mock[ThreadReference]
        val mockThreadInfo = mock[ThreadInfo]
        (mockThreadInfo.toJdiInstance _).expects().returning(mockThread).once()

        val mockMethod = mock[Method]
        val methodInfo = new JavaMethodInfo(
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

        val actual = javaClassTypeInfoProfile.invokeStaticMethod(
          mockThreadInfo,
          methodInfo,
          Nil
        )

        actual should be (expected)
      }

      it("should invoke using the provided arguments") {
        val arguments = Seq(1)
        val mockThread = mock[ThreadReference]
        val mockThreadInfo = mock[ThreadInfo]
        (mockThreadInfo.toJdiInstance _).expects().returning(mockThread).once()

        val mockMethod = mock[Method]
        val methodInfo = new JavaMethodInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockMethod
        )

        val mockVirtualMachine = mock[VirtualMachine]
        val javaClassTypeInfoProfile = new JavaClassTypeInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassType
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfo =
            mockNewTypeProfile(_type)

          override protected def newValueProfile(value: Value): ValueInfo =
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

        javaClassTypeInfoProfile.invokeStaticMethod(
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
        val mockThreadInfo = mock[ThreadInfo]
        (mockThreadInfo.toJdiInstance _).expects().returning(mockThread).once()

        val mockMethod = mock[Method]
        val methodInfo = new JavaMethodInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockMethod
        )

        // Object method is invoked (given flag with value 1)
        (mockClassType.invokeMethod _).expects(*, *, *, 1)
          .returning(mock[Value]).once()

        // Profile is created for return value
        mockNewValueProfile.expects(*).returning(null).once()

        javaClassTypeInfoProfile.invokeStaticMethod(
          mockThreadInfo,
          methodInfo,
          Nil,
          jdiArguments: _*
        )
      }
    }

    describe("#newInstance(thread, constructor, arguments, JDI arguments)") {
      it("should use method(...) to search for constructor with name and signature") {
        val expected = mock[ValueInfo]

        val name = "methodName"
        val signature = "signature"
        val arguments = Seq(3)
        val jdiArguments = Nil

        val mockUnsafeInvoke = mockFunction[
          ThreadInfo,
          MethodInfo,
          Seq[Any],
          Seq[JDIArgument],
          ValueInfo
        ]
        val mockUnsafeMethod = mockFunction[String, String, Option[MethodInfo]]

        val javaClassTypeInfoProfile = new JavaClassTypeInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassType
        ) {
          override def invokeStaticMethod(
            thread: ThreadInfo,
            method: MethodInfo,
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfo = mockUnsafeInvoke(
            thread,
            method,
            arguments,
            jdiArguments
          )

          override def methodOption(
            name: String,
            signature: String
          ): Option[MethodInfo] = mockUnsafeMethod(name, signature)
        }

        val mockThread = mock[ThreadInfo]

        val mockMethod = mock[MethodInfo]
        mockUnsafeMethod.expects(name, signature)
          .returning(Some(mockMethod)).once()

        mockUnsafeInvoke.expects(mockThread, mockMethod, arguments, jdiArguments)
          .returning(expected).once()

        val actual = javaClassTypeInfoProfile.invokeStaticMethod(
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
        val expected = mock[ObjectInfo]
        val mockThread = mock[ThreadReference]
        val mockThreadInfo = mock[ThreadInfo]
        (mockThreadInfo.toJdiInstance _).expects().returning(mockThread).once()

        val mockMethod = mock[Method]
        val methodInfo = new JavaMethodInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockMethod
        )

        val mockVirtualMachine = mock[VirtualMachine]
        val javaClassTypeInfoProfile = new JavaClassTypeInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassType
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfo =
            mockNewTypeProfile(_type)

          override protected def newValueProfile(value: Value): ValueInfo =
            mockNewValueProfile(value)

          override protected def newVirtualMachine(): VirtualMachine =
            mockVirtualMachine

          override protected def newObjectProfile(
            objectReference: ObjectReference,
            virtualMachine: VirtualMachine
          ): ObjectInfo = mockNewObjectProfile(
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

        val actual = javaClassTypeInfoProfile.newInstance(
          mockThreadInfo,
          methodInfo,
          Nil
        )

        actual should be (expected)
      }

      it("should invoke using the provided arguments") {
        val arguments = Seq(1)
        val mockThread = mock[ThreadReference]
        val mockThreadInfo = mock[ThreadInfo]
        (mockThreadInfo.toJdiInstance _).expects().returning(mockThread).once()

        val mockMethod = mock[Method]
        val methodInfo = new JavaMethodInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockMethod
        )

        val mockVirtualMachine = mock[VirtualMachine]
        val javaClassTypeInfoProfile = new JavaClassTypeInfo(
          mockScalaVirtualMachine,
          mockInfoProducerProfile,
          mockClassType
        ) {
          override protected def newTypeProfile(_type: Type): TypeInfo =
            mockNewTypeProfile(_type)

          override protected def newValueProfile(value: Value): ValueInfo =
            mockNewValueProfile(value)

          override protected def newVirtualMachine(): VirtualMachine =
            mockVirtualMachine

          override protected def newObjectProfile(
            objectReference: ObjectReference,
            virtualMachine: VirtualMachine
          ): ObjectInfo = mockNewObjectProfile(
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

        javaClassTypeInfoProfile.newInstance(
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
        val mockThreadInfo = mock[ThreadInfo]
        (mockThreadInfo.toJdiInstance _).expects().returning(mockThread).once()

        val mockMethod = mock[Method]
        val methodInfo = new JavaMethodInfo(
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

        javaClassTypeInfoProfile.newInstance(
          mockThreadInfo,
          methodInfo,
          Nil,
          jdiArguments: _*
        )
      }
    }
  }
}
