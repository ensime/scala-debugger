package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.{TestArrayInfo, TestTypeInfo}

import scala.util.{Failure, Success, Try}

class TypeInfoSpec extends test.ParallelMockFunSpec
{
  describe("TypeInfo") {
    describe("#isBooleanType") {
      it("should return true if is a boolean type") {
        val expected = true

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "Z"
        }

        val actual = typeInfoProfile.isBooleanType

        actual should be (expected)
      }

      it("should return false if is not a boolean type") {
        val expected = false

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "0"
        }

        val actual = typeInfoProfile.isBooleanType

        actual should be (expected)
      }
    }

    describe("#isByteType") {
      it("should return true if is a byte type") {
        val expected = true

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "B"
        }

        val actual = typeInfoProfile.isByteType

        actual should be (expected)
      }

      it("should return false if is not a byte type") {
        val expected = false

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "0"
        }

        val actual = typeInfoProfile.isByteType

        actual should be (expected)
      }
    }

    describe("#isCharType") {
      it("should return true if is a character type") {
        val expected = true

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "C"
        }

        val actual = typeInfoProfile.isCharType

        actual should be (expected)
      }

      it("should return false if is not a character type") {
        val expected = false

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "0"
        }

        val actual = typeInfoProfile.isCharType

        actual should be (expected)
      }
    }

    describe("#isShortType") {
      it("should return true if is a short type") {
        val expected = true

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "S"
        }

        val actual = typeInfoProfile.isShortType

        actual should be (expected)
      }

      it("should return false if is not a short type") {
        val expected = false

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "0"
        }

        val actual = typeInfoProfile.isShortType

        actual should be (expected)
      }
    }

    describe("#isIntegerType") {
      it("should return true if is an integer type") {
        val expected = true

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "I"
        }

        val actual = typeInfoProfile.isIntegerType

        actual should be (expected)
      }

      it("should return false if is not an integer type") {
        val expected = false

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "0"
        }

        val actual = typeInfoProfile.isIntegerType

        actual should be (expected)
      }
    }

    describe("#isLongType") {
      it("should return true if is a long type") {
        val expected = true

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "J"
        }

        val actual = typeInfoProfile.isLongType

        actual should be (expected)
      }

      it("should return false if is not a long type") {
        val expected = false

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "0"
        }

        val actual = typeInfoProfile.isLongType

        actual should be (expected)
      }
    }

    describe("#isFloatType") {
      it("should return true if is a float type") {
        val expected = true

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "F"
        }

        val actual = typeInfoProfile.isFloatType

        actual should be (expected)
      }

      it("should return false if is not a float type") {
        val expected = false

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "0"
        }

        val actual = typeInfoProfile.isFloatType

        actual should be (expected)
      }
    }

    describe("#isDoubleType") {
      it("should return true if is a double type") {
        val expected = true

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "D"
        }

        val actual = typeInfoProfile.isDoubleType

        actual should be (expected)
      }

      it("should return false if is not a double type") {
        val expected = false

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "0"
        }

        val actual = typeInfoProfile.isDoubleType

        actual should be (expected)
      }
    }

    describe("#isStringType") {
      it("should return true if is a string type") {
        val expected = true

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "Ljava/lang/String;"
        }

        val actual = typeInfoProfile.isStringType

        actual should be (expected)
      }

      it("should return false if is not a string type") {
        val expected = false

        val typeInfoProfile = new TestTypeInfo {
          override def signature: String = "0"
        }

        val actual = typeInfoProfile.isStringType

        actual should be (expected)
      }
    }

    describe("#castLocal(AnyVal)") {
      it("should convert the value to a string and invoke castLocal(String)") {
        val expected = new AnyRef
        val value = 33.5

        val mockCastLocal = mockFunction[String, Any]
        val typeInfoProfile = new TestTypeInfo {
          override def castLocal(value: String): Any = mockCastLocal(value)
        }

        mockCastLocal.expects(value.toString).returning(expected).once()

        val actual = typeInfoProfile.castLocal(value)

        actual should be (expected)
      }
    }

    describe("#castLocal(String)") {
      it("should throw an exception if type is not possible to cast") {
        val typeInfoProfile = new TestTypeInfo {
          override def name: String = "0"
          override def isBooleanType: Boolean = false
          override def isByteType: Boolean    = false
          override def isCharType: Boolean    = false
          override def isShortType: Boolean   = false
          override def isIntegerType: Boolean = false
          override def isLongType: Boolean    = false
          override def isFloatType: Boolean   = false
          override def isDoubleType: Boolean  = false
          override def isStringType: Boolean  = false
        }

        intercept[CastNotPossibleException] {
          typeInfoProfile.castLocal("something")
        }
      }

      it("should convert to a boolean if type is boolean") {
        val typeInfoProfile = new TestTypeInfo {
          override def isBooleanType: Boolean = true
          override def isByteType: Boolean    = false
          override def isCharType: Boolean    = false
          override def isShortType: Boolean   = false
          override def isIntegerType: Boolean = false
          override def isLongType: Boolean    = false
          override def isFloatType: Boolean   = false
          override def isDoubleType: Boolean  = false
          override def isStringType: Boolean  = false
        }

        val value = typeInfoProfile.castLocal("true")
        value shouldBe a [java.lang.Boolean]
        (value == true) should be (true)
      }

      it("should convert to a byte if type is byte") {
        val typeInfoProfile = new TestTypeInfo {
          override def isBooleanType: Boolean = false
          override def isByteType: Boolean    = true
          override def isCharType: Boolean    = false
          override def isShortType: Boolean   = false
          override def isIntegerType: Boolean = false
          override def isLongType: Boolean    = false
          override def isFloatType: Boolean   = false
          override def isDoubleType: Boolean  = false
          override def isStringType: Boolean  = false
        }

        val value = typeInfoProfile.castLocal("33")
        value shouldBe a [java.lang.Byte]
        value should be (33)
      }

      it("should convert to a character if type is character") {
        val typeInfoProfile = new TestTypeInfo {
          override def isBooleanType: Boolean = false
          override def isByteType: Boolean    = false
          override def isCharType: Boolean    = true
          override def isShortType: Boolean   = false
          override def isIntegerType: Boolean = false
          override def isLongType: Boolean    = false
          override def isFloatType: Boolean   = false
          override def isDoubleType: Boolean  = false
          override def isStringType: Boolean  = false
        }

        val value = typeInfoProfile.castLocal("33")
        value shouldBe a [java.lang.Character]
        value should be ('3')
      }

      it("should convert to a short if type is short") {
        val typeInfoProfile = new TestTypeInfo {
          override def isBooleanType: Boolean = false
          override def isByteType: Boolean    = false
          override def isCharType: Boolean    = false
          override def isShortType: Boolean   = true
          override def isIntegerType: Boolean = false
          override def isLongType: Boolean    = false
          override def isFloatType: Boolean   = false
          override def isDoubleType: Boolean  = false
          override def isStringType: Boolean  = false
        }

        val value = typeInfoProfile.castLocal("33")
        value shouldBe a [java.lang.Short]
        value should be (33)
      }

      it("should convert to a integer if type is integer") {
        val typeInfoProfile = new TestTypeInfo {
          override def isBooleanType: Boolean = false
          override def isByteType: Boolean    = false
          override def isCharType: Boolean    = false
          override def isShortType: Boolean   = false
          override def isIntegerType: Boolean = true
          override def isLongType: Boolean    = false
          override def isFloatType: Boolean   = false
          override def isDoubleType: Boolean  = false
          override def isStringType: Boolean  = false
        }

        val value = typeInfoProfile.castLocal("33")
        value shouldBe a [java.lang.Integer]
        value should be (33)
      }

      it("should convert to a long if type is long") {
        val typeInfoProfile = new TestTypeInfo {
          override def isBooleanType: Boolean = false
          override def isByteType: Boolean    = false
          override def isCharType: Boolean    = false
          override def isShortType: Boolean   = false
          override def isIntegerType: Boolean = false
          override def isLongType: Boolean    = true
          override def isFloatType: Boolean   = false
          override def isDoubleType: Boolean  = false
          override def isStringType: Boolean  = false
        }

        val value = typeInfoProfile.castLocal("33")
        value shouldBe a [java.lang.Long]
        value should be (33)
      }

      it("should convert to a float if type is float") {
        val typeInfoProfile = new TestTypeInfo {
          override def isBooleanType: Boolean = false
          override def isByteType: Boolean    = false
          override def isCharType: Boolean    = false
          override def isShortType: Boolean   = false
          override def isIntegerType: Boolean = false
          override def isLongType: Boolean    = false
          override def isFloatType: Boolean   = true
          override def isDoubleType: Boolean  = false
          override def isStringType: Boolean  = false
        }

        val value = typeInfoProfile.castLocal("33")
        value shouldBe a [java.lang.Float]
        value should be (33.0)
      }

      it("should convert to a double if type is double") {
        val typeInfoProfile = new TestTypeInfo {
          override def isBooleanType: Boolean = false
          override def isByteType: Boolean    = false
          override def isCharType: Boolean    = false
          override def isShortType: Boolean   = false
          override def isIntegerType: Boolean = false
          override def isLongType: Boolean    = false
          override def isFloatType: Boolean   = false
          override def isDoubleType: Boolean  = true
          override def isStringType: Boolean  = false
        }

        val value = typeInfoProfile.castLocal("33")
        value shouldBe a [java.lang.Double]
        value should be (33.0)
      }

      it("should convert to a string if type is string") {
        val typeInfoProfile = new TestTypeInfo {
          override def isBooleanType: Boolean = false
          override def isByteType: Boolean    = false
          override def isCharType: Boolean    = false
          override def isShortType: Boolean   = false
          override def isIntegerType: Boolean = false
          override def isLongType: Boolean    = false
          override def isFloatType: Boolean   = false
          override def isDoubleType: Boolean  = false
          override def isStringType: Boolean  = true
        }

        val value = typeInfoProfile.castLocal("33")
        value shouldBe a [java.lang.String]
        value should be ("33")
      }

      it("should trim wrapping double quotes if casting to a string") {
        val typeInfoProfile = new TestTypeInfo {
          override def isBooleanType: Boolean = false
          override def isByteType: Boolean    = false
          override def isCharType: Boolean    = false
          override def isShortType: Boolean   = false
          override def isIntegerType: Boolean = false
          override def isLongType: Boolean    = false
          override def isFloatType: Boolean   = false
          override def isDoubleType: Boolean  = false
          override def isStringType: Boolean  = true
        }

        typeInfoProfile.castLocal("test") should be ("test")
        typeInfoProfile.castLocal("\"test") should be ("\"test")
        typeInfoProfile.castLocal("test\"") should be ("test\"")
        typeInfoProfile.castLocal("\"test\"") should be ("test")
      }
    }

    describe("#toPrettyString") {
      it("should include the type name and signature") {
        val expected = "Type NAME (SIGNATURE)"

        val typeInfoProfile = new TestTypeInfo {
          override def name: String = "NAME"
          override def signature: String = "SIGNATURE"
        }

        val actual = typeInfoProfile.toPrettyString

        actual should be (expected)
      }
    }
  }
}
