package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.{InvokeNonVirtualArgument, InvokeSingleThreadedArgument, JDIArgument}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestObjectInfoProfile

class ObjectInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("ObjectInfoProfile") {
    describe("#uniqueIdHexString") {
      it("should return the hex string representing the unique id") {
        val expected = "ABCDE"

        val objectInfoProfile = new TestObjectInfoProfile {
          override def uniqueId: Long = Integer.parseInt(expected, 16)
        }

        val actual = objectInfoProfile.uniqueIdHexString

        actual should be(expected)
      }
    }

    describe("#toPrettyString") {
      it("should display the reference type name and unique id as a hex code") {
        val expected = "Instance of some.class.name (0xABCDE)"

        val mockReferenceTypeInfoProfile = mock[ReferenceTypeInfoProfile]
        (mockReferenceTypeInfoProfile.name _).expects()
          .returning("some.class.name").once()

        val objectInfoProfile = new TestObjectInfoProfile {
          override def uniqueId: Long = Integer.parseInt("ABCDE", 16)
          override def referenceType: ReferenceTypeInfoProfile =
            mockReferenceTypeInfoProfile
        }

        val actual = objectInfoProfile.toPrettyString

        actual should be(expected)
      }
    }

    describe("#tryInvoke(methodProfile, arguments, JDI arguments)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[
          ThreadInfoProfile,
          MethodInfoProfile,
          Seq[Any],
          Seq[JDIArgument],
          ValueInfoProfile
        ]

        val mockThreadInfoProfile = mock[ThreadInfoProfile]

        val objectInfoProfile = new TestObjectInfoProfile {
          override def invoke(
            thread: ThreadInfoProfile,
            methodInfoProfile: MethodInfoProfile,
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfoProfile = mockUnsafeMethod(
            thread,
            methodInfoProfile,
            arguments,
            jdiArguments.toSeq
          )
        }

        val a1 = mockThreadInfoProfile
        val a2 = mock[MethodInfoProfile]
        val a3 = Seq(3, "test", new AnyRef)
        val a4 = Seq(InvokeSingleThreadedArgument, InvokeNonVirtualArgument)
        val r = mock[ValueInfoProfile]

        mockUnsafeMethod.expects(a1, a2, a3, a4).returning(r).once()

        objectInfoProfile.tryInvoke(a1, a2, a3, a4: _*).get should be (r)
      }
    }

    describe("#tryInvoke(methodName, arguments, JDI arguments)") {
      it("should infer parameter types from provided arguments") {
        val mockUnsafeMethod = mockFunction[
          ThreadInfoProfile,
          String,
          Seq[String],
          Seq[Any],
          Seq[JDIArgument],
          ValueInfoProfile
        ]

        val mockThreadInfoProfile = mock[ThreadInfoProfile]

        val objectInfoProfile = new TestObjectInfoProfile {
          override def invoke(
            thread: ThreadInfoProfile,
            methodName: String,
            parameterTypeNames: Seq[String],
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfoProfile = mockUnsafeMethod(
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
        val r = mock[ValueInfoProfile]
        val t = Seq("java.lang.Integer", "java.lang.String", "java.lang.Object")

        mockUnsafeMethod.expects(a1, a2, t, a3, a4).returning(r).once()

        objectInfoProfile.tryInvoke(a1, a2, a3, a4: _*).get should be(r)
      }
    }

    describe("#tryInvoke(methodName, parameter types, arguments, JDI arguments)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[
          ThreadInfoProfile,
          String,
          Seq[String],
          Seq[Any],
          Seq[JDIArgument],
          ValueInfoProfile
        ]

        val mockThreadInfoProfile = mock[ThreadInfoProfile]

        val objectInfoProfile = new TestObjectInfoProfile {
          override def invoke(
            thread: ThreadInfoProfile,
            methodName: String,
            parameterTypeNames: Seq[String],
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfoProfile = mockUnsafeMethod(
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
        val r = mock[ValueInfoProfile]

        mockUnsafeMethod.expects(a1, a2, a3, a4, a5).returning(r).once()

        objectInfoProfile.tryInvoke(a1, a2, a3, a4, a5: _*).get should be (r)
      }
    }

    describe("#invoke(thread, methodName, parameter types, arguments, JDI arguments)") {
      it("should throw an AssertionError if parameter types and arguments are not same length") {
        val objectInfoProfile = new TestObjectInfoProfile

        intercept[AssertionError] {
          objectInfoProfile.invoke(mock[ThreadInfoProfile], "name", Nil, Seq(3))
        }
      }

      it("should use unsafeMethod to search for method with name and parameter types") {
        val expected = mock[ValueInfoProfile]

        val name = "methodName"
        val parameterTypeNames = Seq("some.type")
        val arguments = Seq(3)
        val jdiArguments = Nil

        val mockUnsafeInvoke = mockFunction[
          ThreadInfoProfile,
          MethodInfoProfile,
          Seq[Any],
          Seq[JDIArgument],
          ValueInfoProfile
          ]
        val mockUnsafeMethod = mockFunction[String, Seq[String], MethodInfoProfile]

        val mockThreadInfoProfile = mock[ThreadInfoProfile]

        val objectInfoProfile = new TestObjectInfoProfile {
          override def invoke(
            thread: ThreadInfoProfile,
            methodInfoProfile: MethodInfoProfile,
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfoProfile = mockUnsafeInvoke(
            thread,
            methodInfoProfile,
            arguments,
            jdiArguments
          )
          override def method(
            name: String,
            parameterTypeNames: String*
          ): MethodInfoProfile = mockUnsafeMethod(name, parameterTypeNames)
        }

        val mockMethodInfoProfile = mock[MethodInfoProfile]
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
          ThreadInfoProfile,
          String,
          Seq[String],
          Seq[Any],
          Seq[JDIArgument],
          ValueInfoProfile
        ]

        val mockThreadInfoProfile = mock[ThreadInfoProfile]

        val objectInfoProfile = new TestObjectInfoProfile {
          override def invoke(
            thread: ThreadInfoProfile,
            methodName: String,
            parameterTypeNames: Seq[String],
            arguments: Seq[Any],
            jdiArguments: JDIArgument*
          ): ValueInfoProfile = mockUnsafeMethod(
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
        val r = mock[ValueInfoProfile]
        val t = Seq("java.lang.Integer", "java.lang.String", "java.lang.Object")

        mockUnsafeMethod.expects(a1, a2, t, a3, a4).returning(r).once()

        objectInfoProfile.invoke(a1, a2, a3, a4: _*) should be(r)
      }
    }

    describe("#tryFields") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FieldVariableInfoProfile]]

        val objectInfoProfile = new TestObjectInfoProfile {
          override def fields: Seq[FieldVariableInfoProfile] = mockUnsafeMethod()
        }

        val r = Seq(mock[FieldVariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        objectInfoProfile.tryFields.get should be (r)
      }
    }

    describe("#tryIndexedFields") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[FieldVariableInfoProfile]]

        val objectInfoProfile = new TestObjectInfoProfile {
          override def indexedFields: Seq[FieldVariableInfoProfile] =
            mockUnsafeMethod()
        }

        val r = Seq(mock[FieldVariableInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        objectInfoProfile.tryIndexedFields.get should be (r)
      }
    }

    describe("#tryIndexedField") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, FieldVariableInfoProfile]

        val objectInfoProfile = new TestObjectInfoProfile {
          override def indexedField(name: String): FieldVariableInfoProfile =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[FieldVariableInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        objectInfoProfile.tryIndexedField(a1).get should be (r)
      }
    }

    describe("#indexedField") {
      it("should retrieve the value from indexedFieldOption") {
        val expected = mock[FieldVariableInfoProfile]
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfoProfile]]
        val name = "some name"

        val objectInfoProfile = new TestObjectInfoProfile {
          override def indexedFieldOption(name: String): Option[FieldVariableInfoProfile] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(Some(expected)).once()

        val actual = objectInfoProfile.indexedField(name)

        actual should be (expected)
      }

      it("should throw an exception if indexedFieldOption is None") {
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfoProfile]]
        val name = "some name"

        val objectInfoProfile = new TestObjectInfoProfile {
          override def indexedFieldOption(name: String): Option[FieldVariableInfoProfile] =
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
        val mockUnsafeMethod = mockFunction[String, FieldVariableInfoProfile]

        val objectInfoProfile = new TestObjectInfoProfile {
          override def field(name: String): FieldVariableInfoProfile =
            mockUnsafeMethod(name)
        }

        val a1 = "someName"
        val r = mock[FieldVariableInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        objectInfoProfile.tryField(a1).get should be (r)
      }
    }

    describe("#field") {
      it("should retrieve the value from fieldOption") {
        val expected = mock[FieldVariableInfoProfile]
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfoProfile]]
        val name = "some name"

        val objectInfoProfile = new TestObjectInfoProfile {
          override def fieldOption(name: String): Option[FieldVariableInfoProfile] =
            mockOptionMethod(name)
        }

        mockOptionMethod.expects(name).returning(Some(expected)).once()

        val actual = objectInfoProfile.field(name)

        actual should be (expected)
      }

      it("should throw an exception if fieldOption is None") {
        val mockOptionMethod = mockFunction[String, Option[FieldVariableInfoProfile]]
        val name = "some name"

        val objectInfoProfile = new TestObjectInfoProfile {
          override def fieldOption(name: String): Option[FieldVariableInfoProfile] =
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
        val mockUnsafeMethod = mockFunction[Seq[MethodInfoProfile]]

        val objectInfoProfile = new TestObjectInfoProfile {
          override def methods: Seq[MethodInfoProfile] = mockUnsafeMethod()
        }

        val r = Seq(mock[MethodInfoProfile])
        mockUnsafeMethod.expects().returning(r).once()
        objectInfoProfile.tryMethods.get should be (r)
      }
    }

    describe("#tryMethod") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, Seq[String], MethodInfoProfile]

        val objectInfoProfile = new TestObjectInfoProfile {
          override def method(
            name: String,
            parameterTypeNames: String*
          ): MethodInfoProfile = mockUnsafeMethod(name, parameterTypeNames)
        }

        val a1 = "someName"
        val a2 = Seq("param.type")
        val r = mock[MethodInfoProfile]
        mockUnsafeMethod.expects(a1, a2).returning(r).once()
        objectInfoProfile.tryMethod(a1, a2: _*).get should be (r)
      }
    }

    describe("#method") {
      it("should retrieve the value from methodOption") {
        val expected = mock[MethodInfoProfile]
        val mockOptionMethod = mockFunction[String, Seq[String], Option[MethodInfoProfile]]
        val name = "some name"
        val parameterTypeNames = Seq("one", "two")

        val objectInfoProfile = new TestObjectInfoProfile {
          override def methodOption(
            name: String,
            parameterTypeNames: String*
          ): Option[MethodInfoProfile] = mockOptionMethod(name, parameterTypeNames)
        }

        mockOptionMethod.expects(name, parameterTypeNames)
          .returning(Some(expected)).once()

        val actual = objectInfoProfile.method(name, parameterTypeNames: _*)

        actual should be (expected)
      }

      it("should throw an exception if methodOption is None") {
        val mockOptionMethod = mockFunction[String, Seq[String], Option[MethodInfoProfile]]
        val name = "some name"
        val parameterTypeNames = Seq("one", "two")

        val objectInfoProfile = new TestObjectInfoProfile {
          override def methodOption(
            name: String,
            parameterTypeNames: String*
          ): Option[MethodInfoProfile] = mockOptionMethod(name, parameterTypeNames)
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
