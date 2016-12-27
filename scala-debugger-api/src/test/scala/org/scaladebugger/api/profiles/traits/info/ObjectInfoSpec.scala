package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.{InvokeNonVirtualArgument, InvokeSingleThreadedArgument, JDIArgument}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestObjectInfo

class ObjectInfoSpec extends ParallelMockFunSpec
{
  describe("ObjectInfo") {
    describe("#uniqueIdHexString") {
      it("should return the hex string representing the unique id") {
        val expected = "ABCDE"

        val objectInfoProfile = new TestObjectInfo {
          override def uniqueId: Long = Integer.parseInt(expected, 16)
        }

        val actual = objectInfoProfile.uniqueIdHexString

        actual should be(expected)
      }
    }

    describe("#toPrettyString") {
      it("should display the reference type name and unique id as a hex code") {
        val expected = "Instance of some.class.name (0xABCDE)"

        val mockReferenceTypeInfoProfile = mock[ReferenceTypeInfo]
        (mockReferenceTypeInfoProfile.name _).expects()
          .returning("some.class.name").once()

        val objectInfoProfile = new TestObjectInfo {
          override def uniqueId: Long = Integer.parseInt("ABCDE", 16)
          override def referenceType: ReferenceTypeInfo =
            mockReferenceTypeInfoProfile
        }

        val actual = objectInfoProfile.toPrettyString

        actual should be(expected)
      }
    }

    describe("#tryInvoke(methodProfile, arguments, JDI arguments)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[
          ThreadInfo,
          MethodInfo,
          Seq[Any],
          Seq[JDIArgument],
          ValueInfo
        ]

        val mockThreadInfoProfile = mock[ThreadInfo]

        val objectInfoProfile = new TestObjectInfo {
          override def invoke(
            thread: ThreadInfo,
            methodInfoProfile: MethodInfo,
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfo = mockUnsafeMethod(
            thread,
            methodInfoProfile,
            arguments,
            jdiArguments.toSeq
          )
        }

        val a1 = mockThreadInfoProfile
        val a2 = mock[MethodInfo]
        val a3 = Seq(3, "test", new AnyRef)
        val a4 = Seq(InvokeSingleThreadedArgument, InvokeNonVirtualArgument)
        val r = mock[ValueInfo]

        mockUnsafeMethod.expects(a1, a2, a3, a4).returning(r).once()

        objectInfoProfile.tryInvoke(a1, a2, a3, a4: _*).get should be (r)
      }
    }

    describe("#tryInvoke(methodName, arguments, JDI arguments)") {
      it("should infer parameter types from provided arguments") {
        val mockUnsafeMethod = mockFunction[
          ThreadInfo,
          String,
          Seq[String],
          Seq[Any],
          Seq[JDIArgument],
          ValueInfo
        ]

        val mockThreadInfoProfile = mock[ThreadInfo]

        val objectInfoProfile = new TestObjectInfo {
          override def invoke(
            thread: ThreadInfo,
            methodName: String,
            parameterTypeNames: Seq[String],
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfo = mockUnsafeMethod(
            thread,
            methodName,
            parameterTypeNames,
            arguments,
            jdiArguments.toSeq
          )
        }

        val a1 = mockThreadInfoProfile
        val a2 = "some method name"
        val a3 = Seq(3, "test", new AnyRef)
        val a4 = Seq(InvokeSingleThreadedArgument, InvokeNonVirtualArgument)
        val r = mock[ValueInfo]
        val t = Seq("java.lang.Integer", "java.lang.String", "java.lang.Object")

        mockUnsafeMethod.expects(a1, a2, t, a3, a4).returning(r).once()

        objectInfoProfile.tryInvoke(a1, a2, a3, a4: _*).get should be(r)
      }
    }

    describe("#tryInvoke(methodName, parameter types, arguments, JDI arguments)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[
          ThreadInfo,
          String,
          Seq[String],
          Seq[Any],
          Seq[JDIArgument],
          ValueInfo
        ]

        val mockThreadInfoProfile = mock[ThreadInfo]

        val objectInfoProfile = new TestObjectInfo {
          override def invoke(
            thread: ThreadInfo,
            methodName: String,
            parameterTypeNames: Seq[String],
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfo = mockUnsafeMethod(
            thread,
            methodName,
            parameterTypeNames,
            arguments,
            jdiArguments.toSeq
          )
        }

        val a1 = mockThreadInfoProfile
        val a2 = "some method name"
        val a3 = Seq("some", "parameter", "types")
        val a4 = Seq(3, "test", new AnyRef)
        val a5 = Seq(InvokeSingleThreadedArgument, InvokeNonVirtualArgument)
        val r = mock[ValueInfo]

        mockUnsafeMethod.expects(a1, a2, a3, a4, a5).returning(r).once()

        objectInfoProfile.tryInvoke(a1, a2, a3, a4, a5: _*).get should be (r)
      }
    }

    describe("#invoke(thread, methodName, parameter types, arguments, JDI arguments)") {
      it("should throw an AssertionError if parameter types and arguments are not same length") {
        val objectInfoProfile = new TestObjectInfo

        intercept[AssertionError] {
          objectInfoProfile.invoke(mock[ThreadInfo], "name", Nil, Seq(3))
        }
      }

      it("should use unsafeMethod to search for method with name and parameter types") {
        val expected = mock[ValueInfo]

        val name = "methodName"
        val parameterTypeNames = Seq("some.type")
        val arguments = Seq(3)
        val jdiArguments = Nil

        val mockUnsafeInvoke = mockFunction[
          ThreadInfo,
          MethodInfo,
          Seq[Any],
          Seq[JDIArgument],
          ValueInfo
          ]
        val mockUnsafeMethod = mockFunction[String, Seq[String], MethodInfo]

        val mockThreadInfoProfile = mock[ThreadInfo]

        val objectInfoProfile = new TestObjectInfo {
          override def invoke(
            thread: ThreadInfo,
            methodInfoProfile: MethodInfo,
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfo = mockUnsafeInvoke(
            thread,
            methodInfoProfile,
            arguments,
            jdiArguments
          )
          override def method(
            name: String,
            parameterTypeNames: String*
          ): MethodInfo = mockUnsafeMethod(name, parameterTypeNames)
        }

        val mockMethodInfoProfile = mock[MethodInfo]
        mockUnsafeMethod.expects(name, parameterTypeNames)
          .returning(mockMethodInfoProfile).once()

        mockUnsafeInvoke.expects(
          mockThreadInfoProfile,
          mockMethodInfoProfile,
          arguments,
          jdiArguments
        ).returning(expected).once()

        val actual = objectInfoProfile.invoke(
          mockThreadInfoProfile,
          name,
          parameterTypeNames,
          arguments,
          jdiArguments: _*
        )

        actual should be (expected)
      }
    }

    describe("#invoke(thread, methodName, arguments, JDI arguments)") {
      it("should infer parameter types from provided arguments") {
        val mockUnsafeMethod = mockFunction[
          ThreadInfo,
          String,
          Seq[String],
          Seq[Any],
          Seq[JDIArgument],
          ValueInfo
        ]

        val mockThreadInfoProfile = mock[ThreadInfo]

        val objectInfoProfile = new TestObjectInfo {
          override def invoke(
            thread: ThreadInfo,
            methodName: String,
            parameterTypeNames: Seq[String],
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfo = mockUnsafeMethod(
            thread,
            methodName,
            parameterTypeNames,
            arguments,
            jdiArguments.toSeq
          )
        }

        val a1 = mockThreadInfoProfile
        val a2 = "some method name"
        val a3 = Seq(3, "test", new AnyRef)
        val a4 = Seq(InvokeSingleThreadedArgument, InvokeNonVirtualArgument)
        val r = mock[ValueInfo]
        val t = Seq("java.lang.Integer", "java.lang.String", "java.lang.Object")

        mockUnsafeMethod.expects(a1, a2, t, a3, a4).returning(r).once()

        objectInfoProfile.invoke(a1, a2, a3, a4: _*) should be(r)
      }
    }

    describe("#tryFields") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FieldVariableInfo]]

        val objectInfoProfile = new TestObjectInfo {
          override def fields: Seq[FieldVariableInfo] = mockUnsafeMethod()
        }

        val r = Seq(mock[FieldVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        objectInfoProfile.tryFields.get should be (r)
      }
    }

    describe("#tryIndexedFields") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FieldVariableInfo]]

        val objectInfoProfile = new TestObjectInfo {
          override def indexedFields: Seq[FieldVariableInfo] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[FieldVariableInfo])
        mockUnsafeMethod.expects().returning(r).once()
        objectInfoProfile.tryIndexedFields.get should be (r)
      }
    }

    describe("#tryIndexedField") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, FieldVariableInfo]

        val objectInfoProfile = new TestObjectInfo {
          override def indexedField(name: String): FieldVariableInfo =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[FieldVariableInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        objectInfoProfile.tryIndexedField(a1).get should be (r)
      }
    }

    describe("#indexedField") {
      it("should retrieve the value from indexedFieldOption") {
        val expected = mock[FieldVariableInfo]
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfo]]
        val name = "some name"

        val objectInfoProfile = new TestObjectInfo {
          override def indexedFieldOption(name: String): Option[FieldVariableInfo] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(Some(expected)).once()

        val actual = objectInfoProfile.indexedField(name)

        actual should be (expected)
      }

      it("should throw an exception if indexedFieldOption is None") {
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfo]]
        val name = "some name"

        val objectInfoProfile = new TestObjectInfo {
          override def indexedFieldOption(name: String): Option[FieldVariableInfo] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(None).once()

        intercept[NoSuchElementException] {
          objectInfoProfile.indexedField(name)
        }
      }
    }

    describe("#tryField") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, FieldVariableInfo]

        val objectInfoProfile = new TestObjectInfo {
          override def field(name: String): FieldVariableInfo =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[FieldVariableInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        objectInfoProfile.tryField(a1).get should be (r)
      }
    }

    describe("#field") {
      it("should retrieve the value from fieldOption") {
        val expected = mock[FieldVariableInfo]
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfo]]
        val name = "some name"

        val objectInfoProfile = new TestObjectInfo {
          override def fieldOption(name: String): Option[FieldVariableInfo] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(Some(expected)).once()

        val actual = objectInfoProfile.field(name)

        actual should be (expected)
      }

      it("should throw an exception if fieldOption is None") {
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfo]]
        val name = "some name"

        val objectInfoProfile = new TestObjectInfo {
          override def fieldOption(name: String): Option[FieldVariableInfo] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(None).once()

        intercept[NoSuchElementException] {
          objectInfoProfile.field(name)
        }
      }
    }

    describe("#tryMethods") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[MethodInfo]]

        val objectInfoProfile = new TestObjectInfo {
          override def methods: Seq[MethodInfo] = mockUnsafeMethod()
        }

        val r = Seq(mock[MethodInfo])
        mockUnsafeMethod.expects().returning(r).once()
        objectInfoProfile.tryMethods.get should be (r)
      }
    }

    describe("#tryMethod") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, Seq[String], MethodInfo]

        val objectInfoProfile = new TestObjectInfo {
          override def method(
            name: String,
            parameterTypeNames: String*
          ): MethodInfo = mockUnsafeMethod(name, parameterTypeNames)
        }

        val a1 = "someName"
        val a2 = Seq("param.type")
        val r = mock[MethodInfo]
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        objectInfoProfile.tryMethod(a1, a2: _*).get should be (r)
      }
    }

    describe("#method") {
      it("should retrieve the value from methodOption") {
        val expected = mock[MethodInfo]
        val mockOptionMethod = mockFunction[String, Seq[String], Option[MethodInfo]]
        val name = "some name"
        val parameterTypeNames = Seq("one", "two")

        val objectInfoProfile = new TestObjectInfo {
          override def methodOption(
            name: String,
            parameterTypeNames: String*
          ): Option[MethodInfo] = mockOptionMethod(name, parameterTypeNames)
        }

        mockOptionMethod.expects(name, parameterTypeNames)
          .returning(Some(expected)).once()

        val actual = objectInfoProfile.method(name, parameterTypeNames: _*)

        actual should be (expected)
      }

      it("should throw an exception if methodOption is None") {
        val mockOptionMethod = mockFunction[String, Seq[String], Option[MethodInfo]]
        val name = "some name"
        val parameterTypeNames = Seq("one", "two")

        val objectInfoProfile = new TestObjectInfo {
          override def methodOption(
            name: String,
            parameterTypeNames: String*
          ): Option[MethodInfo] = mockOptionMethod(name, parameterTypeNames)
        }

        mockOptionMethod.expects(name, parameterTypeNames)
          .returning(None).once()

        intercept[NoSuchElementException] {
          objectInfoProfile.method(name, parameterTypeNames: _*)
        }
      }
    }
  }
}
