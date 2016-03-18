package org.scaladebugger.language.interpreters
import acyclic.file

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.Charset

import org.scaladebugger.language.models.Undefined
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.{Failure, Success}

class DebuggerInterpreterIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution
{
  private val interpreter = new DebuggerInterpreter

  describe("DebuggerInterpreter") {
    describe("variables") {
      they("should return their assigned value upon assignment") {
        val expected = Success(3)
        val actual = interpreter.interpret("x := 3")

        actual should be (expected)
      }

      they("should return their assigned value upon usage") {
        val expected = Success(3)

        interpreter.interpret("x := 3")
        val actual = interpreter.interpret("x")

        actual should be (expected)
      }

      they("should support being updated") {
        val expected = Success(4)

        interpreter.interpret("x := 3")
        val actual = interpreter.interpret("x := x + 1")

        actual should be (expected)
      }

      they("should support updating from a different scope") {
        val expected = Success(4)

        val actual = interpreter.interpret("x:=3;{x:=x+1};x")

        actual should be (expected)
      }

      they("should fail when accessed before assigned") {
        interpreter.interpret("x") shouldBe a [Failure[_]]
      }
    }

    describe("functions") {
      they("should be returned from a function declaration") {
        val expected = Success(5)

        interpreter.interpret("x:=func(){5}")
        val actual = interpreter.interpret("x()")

        actual should be (expected)
      }

      they("should be returned as string blobs from the interpreter") {
        val result = interpreter.interpret("func(){}").get

        result shouldBe a [String]
        result.asInstanceOf[String] should include ("<INTERPRETED>")
      }

      they("should support being invoked from inline declaration") {
        val expected = Success(4)

        val actual = interpreter.interpret("(func(){7-3})()")

        actual should be (expected)
      }

      they("should support being invoked with no arguments not using ()") {
        val expected = Success(4)

        val actual = interpreter.interpret("x:=func(){7-3};x")

        actual should be (expected)
      }

      they("should support being invoked with no arguments using ()") {
        val expected = Success(4)

        val actual = interpreter.interpret("x:=func(){7-3};x()")

        actual should be (expected)
      }

      they("should support being invoked with ordered, nameless arguments") {
        val expected = Success(4)

        val actual = interpreter.interpret("x:=func(a,b){a-b};x(7,3)")

        actual should be (expected)
      }

      they("should support being invoked with named arguments") {
        val expected = Success(4)

        val actual = interpreter.interpret("x:=func(a,b){a-b};x(b=3,a=7)")

        actual should be (expected)
      }

      they("should support being invoked with ordered and named arguments") {
        val expected = Success(4)

        val actual = interpreter.interpret("x:=func(a,b){a-b};x(7,b=3)")

        actual should be (expected)
      }

      they("should fill in any missing arguments with undefined") {
        val expected = Success(Undefined.toScalaValue)

        val actual = interpreter.interpret("x:=func(a){a};x()")

        actual should be (expected)
      }

      they("should fail when invoked with ordered and named arguments in wrong order") {
        interpreter.interpret("x:=func(a,b){a-b};x(b=3,7)") shouldBe a [Failure[_]]
      }

      they("should evaluate arguments of an invocation") {
        val expected = Success(4)

        interpreter.interpret("x:=func(a,b){a-b}")
        val actual = interpreter.interpret("x(10-3,1+2)")

        actual should be (expected)
      }

      they("should invoke functions passed as arguments to an invocation") {
        val expected = Success(4)

        interpreter.interpret("x:=func(){7};y:=func(){3};z:=func(a,b){a-b}")
        val actual = interpreter.interpret("z(x,y)")

        actual should be (expected)
      }

      they("should support being invoked with a single argument that is a function") {
        val expected = Success(4)

        interpreter.interpret("x:=func(){4};y:=func(a){a}")
        val actual = interpreter.interpret("y x")

        actual should be (expected)
      }

      they("should support native functions with unit result returning as undefined") {
        val expected = Success(Undefined.toScalaValue)

        interpreter.bindFunction("test", Nil, m => {})
        val actual = interpreter.interpret("test")

        actual should be (expected)
      }

      they("should support closures") {
        val expected = Success(5)

        interpreter.interpret("x:=(func(){y:=0;func(){y:=y+1}})()")
        val actual = interpreter.interpret("x;x;x;x;x")

        actual should be (expected)
      }

      they("should support being defined and used inside blocks") {
        val expected = Success(4)

        val actual = interpreter.interpret("{x:=func(){4};x}")

        actual should be (expected)
      }
    }

    describe("blocks") {
      they("should create a new scope and not pollute the parent scope") {
        interpreter.interpret("x:=10")
        interpreter.interpret("{y:=999}")
        interpreter.interpret("y").isFailure should be (true)
      }

      they("should evaluate each contained expression") {
        val expected = Success(2)

        val actual = interpreter.interpret("{x:=0;x:=x+1;x:=x+1}")

        actual should be (expected)
      }

      they("should return the result of the last expression") {
        val expected = Success("test")

        val actual = interpreter.interpret("{3;4*5;\"test\"}")

        actual should be (expected)
      }

      they("should return undefined if no expressions are provided") {
        val expected = Success(Undefined.toScalaValue)

        val actual = interpreter.interpret("{}")

        actual should be (expected)
      }
    }

    describe("conditionals") {
      they("should return the result of the truthy expression if condition is true") {
        val expected = Success(999)

        val actual = interpreter.interpret("if (3 == 3) 999 else 111")

        actual should be (expected)
      }

      they("should return the result of the falsey expression if condition is false") {
        val expected = Success(111)

        val actual = interpreter.interpret("if (3 != 3) 999 else 111")

        actual should be (expected)
      }

      they("should evaluate the conditional expression") {
        val expected = Success(999)

        interpreter.interpret("x:=999")
        val actual = interpreter.interpret("if (x==999) x else 111")

        actual should be (expected)
      }

      they("should evaluate the truthy expression if necessary") {
        val expected = Success(2)

        val actual = interpreter.interpret("if (3 == 3) {x:=1;x:=x+1} else 0")

        actual should be (expected)
      }

      they("should evaluate the falsey expression if necessary") {
        val expected = Success(2)

        val actual = interpreter.interpret("if (3 != 3) 0 else {x:=1;x:=x+1}")

        actual should be (expected)
      }
    }

    describe("builtin values") {
      describe("true") {
        val expected = Success(true)

        val actual = interpreter.interpret("true")

        actual should be (expected)
      }

      describe("false") {
        it("should yield false when returned from interpreter") {
          val expected = Success(false)

          val actual = interpreter.interpret("false")

          actual should be (expected)
        }
      }

      describe("undefined") {
        it("should yield the string 'undefined' when returned from interpreter") {
          val expected = Success(Undefined.toScalaValue)

          val actual = interpreter.interpret("undefined")

          actual should be (expected)
        }

        it("should only match other undefined") {
          interpreter.interpret("undefined == undefined") should be (Success(true))
          interpreter.interpret("undefined != undefined") should be (Success(false))
          interpreter.interpret("\"undefined\" == undefined") should be (Success(false))
          interpreter.interpret("0 == undefined") should be (Success(false))
          interpreter.interpret("x:=undefined;x==undefined") should be (Success(true))
        }
      }
    }
    
    describe("builtin functions") {
      describe("print") {
        it("should print the value to the interpreter's out stream") {
          val expected = "some output" + System.getProperty("line.separator")

          val bs = new ByteArrayOutputStream
          val interpreter = new DebuggerInterpreter(out = new PrintStream(bs))

          interpreter.interpret(s"""print("some output")""")

          val actual = bs.toString(Charset.forName("UTF-8").name())

          actual should be (expected)
        }
      }

      describe("printErr") {
        it("should print the value to the interpreter's error stream") {
          val expected = "some output" + System.getProperty("line.separator")

          val bs = new ByteArrayOutputStream
          val interpreter = new DebuggerInterpreter(err = new PrintStream(bs))

          interpreter.interpret(s"""printErr("some output")""")

          val actual = bs.toString(Charset.forName("UTF-8").name())

          actual should be (expected)
        }
      }

      describe("plusPlus") {
        it("should combine two strings together") {
          val expected = Success("helloworld")

          val actual = interpreter.interpret("plusPlus(\"hello\",\"world\")")

          actual should be (expected)
        }

        it("should combine a number and string as a string") {
          val expected = Success("5.0world")

          val actual = interpreter.interpret("plusPlus(5,\"world\")")

          actual should be (expected)
        }

        it("should combine two numbers as strings") {
          val expected = Success("5.05.0")

          val actual = interpreter.interpret("plusPlus(5,5)")

          actual should be (expected)
        }

        it("should use l and r for argument names") {
          val expected = Success("7.03.0")

          val actual = interpreter.interpret("plusPlus(r=3,l=7)")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a string") {
          interpreter.interpret("plusPlus(\"abc\",func(){})") shouldBe a [Failure[_]]
        }
      }

      describe("plus") {
        it("should add two numbers together") {
          val expected = Success(10)

          val actual = interpreter.interpret("plus(7,3)")

          actual should be (expected)
        }

        it("should use l and r for argument names") {
          val expected = Success(10)

          val actual = interpreter.interpret("plus(r=3,l=7)")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("plus(\"abc\",5)") shouldBe a [Failure[_]]
        }
      }

      describe("minus") {
        it("should subtract the right number from the left") {
          val expected = Success(4)

          val actual = interpreter.interpret("minus(7,3)")

          actual should be (expected)
        }

        it("should use l and r for argument names") {
          val expected = Success(4)

          val actual = interpreter.interpret("minus(r=3,l=7)")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("minus(\"abc\",5)") shouldBe a [Failure[_]]
        }
      }

      describe("multiply") {
        it("should multiply two numbers together") {
          val expected = Success(21)

          val actual = interpreter.interpret("multiply(7,3)")

          actual should be (expected)
        }

        it("should use l and r for argument names") {
          val expected = Success(21)

          val actual = interpreter.interpret("multiply(r=3,l=7)")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("multiply(\"abc\",5)") shouldBe a [Failure[_]]
        }
      }

      describe("divide") {
        it("should divide the left number by the right") {
          val expected = Success(7.0/3.0)

          val actual = interpreter.interpret("divide(7,3)")

          actual should be (expected)
        }

        it("should use l and r for argument names") {
          val expected = Success(7.0/3.0)

          val actual = interpreter.interpret("divide(r=3,l=7)")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("minus(\"abc\",5)") shouldBe a [Failure[_]]
        }
      }

      describe("modulus") {
        it("should divide the left number by the right and return remainder") {
          val expected = Success(1)

          val actual = interpreter.interpret("modulus(7,3)")

          actual should be (expected)
        }

        it("should use l and r for argument names") {
          val expected = Success(1)

          val actual = interpreter.interpret("modulus(r=3,l=7)")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("modulus(\"abc\",5)") shouldBe a [Failure[_]]
        }
      }

      describe("lessThan") {
        it("should evaluate to true if left is less than right") {
          val expected = Success(true)

          val actual = interpreter.interpret("lessThan(0,3)")

          actual should be (expected)
        }

        it("should evaluate to false if left is equal to right") {
          val expected = Success(false)

          val actual = interpreter.interpret("lessThan(3,3)")

          actual should be (expected)
        }

        it("should evaluate to false if left is greater than right") {
          val expected = Success(false)

          val actual = interpreter.interpret("lessThan(7,3)")

          actual should be (expected)
        }

        it("should use l and r for argument names") {
          val expected = Success(true)

          val actual = interpreter.interpret("lessThan(r=7,l=3)")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("lessThan(\"abc\",5)") shouldBe a [Failure[_]]
        }
      }

      describe("lessThanEqual") {
        it("should evaluate to true if left is less than right") {
          val expected = Success(true)

          val actual = interpreter.interpret("lessThanEqual(0,3)")

          actual should be (expected)
        }

        it("should evaluate to true if left is equal to right") {
          val expected = Success(true)

          val actual = interpreter.interpret("lessThanEqual(3,3)")

          actual should be (expected)
        }

        it("should evaluate to false if left is greater than right") {
          val expected = Success(false)

          val actual = interpreter.interpret("lessThanEqual(7,3)")

          actual should be (expected)
        }

        it("should use l and r for argument names") {
          val expected = Success(true)

          val actual = interpreter.interpret("lessThanEqual(r=7,l=3)")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("lessThanEqual(\"abc\",5)") shouldBe a [Failure[_]]
        }
      }

      describe("greaterThan") {
        it("should evaluate to false if left is less than right") {
          val expected = Success(false)

          val actual = interpreter.interpret("greaterThan(0,3)")

          actual should be (expected)
        }

        it("should evaluate to false if left is equal to right") {
          val expected = Success(false)

          val actual = interpreter.interpret("greaterThan(3,3)")

          actual should be (expected)
        }

        it("should evaluate to true if left is greater than right") {
          val expected = Success(true)

          val actual = interpreter.interpret("greaterThan(7,3)")

          actual should be (expected)
        }

        it("should use l and r for argument names") {
          val expected = Success(true)

          val actual = interpreter.interpret("greaterThan(r=3,l=7)")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("greaterThan(\"abc\",5)") shouldBe a [Failure[_]]
        }
      }

      describe("greaterThanEqual") {
        it("should evaluate to false if left is less than right") {
          val expected = Success(false)

          val actual = interpreter.interpret("greaterThanEqual(0,3)")

          actual should be (expected)
        }

        it("should evaluate to true if left is equal to right") {
          val expected = Success(true)

          val actual = interpreter.interpret("greaterThanEqual(3,3)")

          actual should be (expected)
        }

        it("should evaluate to true if left is greater than right") {
          val expected = Success(true)

          val actual = interpreter.interpret("greaterThanEqual(7,3)")

          actual should be (expected)
        }

        it("should use l and r for argument names") {
          val expected = Success(true)

          val actual = interpreter.interpret("greaterThanEqual(r=3,l=7)")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("greaterThanEqual(\"abc\",5)") shouldBe a [Failure[_]]
        }
      }

      describe("equal") {
        it("should evaluate to true if left is equal to right (numbers)") {
          val expected = Success(true)

          val actual = interpreter.interpret("3==3")

          actual should be (expected)
        }

        it("should evaluate to true if left is equal to right (text)") {
          val expected = Success(true)

          val actual = interpreter.interpret("\"hello\"==\"hello\"")

          actual should be (expected)
        }

        it("should support evaluating expressions") {
          val expected = Success(true)

          val actual = interpreter.interpret("x:=3;y:=3;x==y")

          actual should be (expected)
        }

        it("should use l and r for argument names") {
          val expected = Success(true)

          val actual = interpreter.interpret("equal(r=3,l=3)")

          actual should be (expected)
        }

        it("should support comparing different types") {
          val expected = Success(false)

          val actual = interpreter.interpret("equal(\"abc\",5)")

          actual should be (expected)
        }
      }

      describe("notEqual") {
        it("should evaluate to true if left is not equal to right (numbers)") {
          val expected = Success(true)

          val actual = interpreter.interpret("notEqual(3,4)")

          actual should be (expected)
        }

        it("should evaluate to true if left is not equal to right (text)") {
          val expected = Success(true)

          val actual = interpreter.interpret("notEqual(\"hello\",\"world\")")

          actual should be (expected)
        }

        it("should evaluate to false if left is equal to right") {
          val expected = Success(false)

          val actual = interpreter.interpret("notEqual(3,3)")

          actual should be (expected)
        }

        it("should use l and r for argument names") {
          val expected = Success(true)

          val actual = interpreter.interpret("notEqual(r=3,l=7)")

          actual should be (expected)
        }

        it("should support comparing different types") {
          val expected = Success(true)

          val actual = interpreter.interpret("notEqual(\"abc\",5)")

          actual should be (expected)
        }
      }
    }

    describe("operations") {
      describe("@") {
        it("should skip evaluation once for the provided expression") {
          val expected = Success(4)

          val actual = interpreter.interpret("y:=999;x:=@(y-3);y:=7;x")

          actual should be (expected)
        }

        it("should be fully evaluated if returned from the interpreter") {
          val expected = Success(996)

          val actual = interpreter.interpret("y:=999;@(@(@(y-3)))")

          actual should be (expected)
        }

        it("should support function calls over identifiers") {
          val expected = Success(4)

          val actual = interpreter.interpret("x:=func(){4};y:=@x;y")

          actual should be (expected)
        }
      }

      describe("+") {
        it("should add two numbers together") {
          val expected = Success(10)

          val actual = interpreter.interpret("7+3")

          actual should be (expected)
        }

        it("should support evaluating expressions") {
          val expected = Success(10)

          val actual = interpreter.interpret("x:=7;y:=3;x+y")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("\"abc\"+5") shouldBe a [Failure[_]]
        }
      }

      describe("-") {
        it("should subtract the right number from the left") {
          val expected = Success(4)

          val actual = interpreter.interpret("7-3")

          actual should be (expected)
        }

        it("should support evaluating expressions") {
          val expected = Success(4)

          val actual = interpreter.interpret("x:=7;y:=3;x-y")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("\"abc\"-5") shouldBe a [Failure[_]]
        }
      }

      describe("++") {
        it("should combine two strings together") {
          val expected = Success("helloworld")

          val actual = interpreter.interpret("\"hello\"++\"world\"")

          actual should be (expected)
        }

        it("should combine a number and string as a string") {
          val expected = Success("5.0world")

          val actual = interpreter.interpret("5++\"world\"")

          actual should be (expected)
        }

        it("should combine two numbers as strings") {
          val expected = Success("5.05.0")

          val actual = interpreter.interpret("5++5")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a string") {
          interpreter.interpret("\"abc\"++func(){}") shouldBe a [Failure[_]]
        }
      }

      describe("*") {
        it("should multiply two numbers together") {
          val expected = Success(21)

          val actual = interpreter.interpret("7*3")

          actual should be (expected)
        }

        it("should support evaluating expressions") {
          val expected = Success(21)

          val actual = interpreter.interpret("x:=7;y:=3;x*y")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("\"abc\"*5") shouldBe a [Failure[_]]
        }
      }

      describe("/") {
        it("should divide the left number by the right") {
          val expected = Success(7.0/3.0)

          val actual = interpreter.interpret("7/3")

          actual should be (expected)
        }

        it("should support evaluating expressions") {
          val expected = Success(7.0/3.0)

          val actual = interpreter.interpret("x:=7;y:=3;x/y")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("\"abc\"/5") shouldBe a [Failure[_]]
        }
      }

      describe("%") {
        it("should divide the left number by the right and return remainder") {
          val expected = Success(1)

          val actual = interpreter.interpret("7%3")

          actual should be (expected)
        }

        it("should support evaluating expressions") {
          val expected = Success(1)

          val actual = interpreter.interpret("x:=7;y:=3;x%y")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("\"abc\"%5") shouldBe a [Failure[_]]
        }
      }

      describe("<") {
        it("should evaluate to true if left is less than right") {
          val expected = Success(true)

          val actual = interpreter.interpret("0<3")

          actual should be (expected)
        }

        it("should evaluate to false if left is equal to right") {
          val expected = Success(false)

          val actual = interpreter.interpret("3<3")

          actual should be (expected)
        }

        it("should evaluate to false if left is greater than right") {
          val expected = Success(false)

          val actual = interpreter.interpret("7<3")

          actual should be (expected)
        }

        it("should support evaluating expressions") {
          val expected = Success(true)

          val actual = interpreter.interpret("x:=0;y:=3;x<y")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("\"abc\"<5") shouldBe a [Failure[_]]
        }
      }

      describe("<=") {
        it("should evaluate to true if left is less than right") {
          val expected = Success(true)

          val actual = interpreter.interpret("0<=3")

          actual should be (expected)
        }

        it("should evaluate to true if left is equal to right") {
          val expected = Success(true)

          val actual = interpreter.interpret("3<=3")

          actual should be (expected)
        }

        it("should evaluate to false if left is greater than right") {
          val expected = Success(false)

          val actual = interpreter.interpret("7<=3")

          actual should be (expected)
        }

        it("should support evaluating expressions") {
          val expected = Success(true)

          val actual = interpreter.interpret("x:=3;y:=3;x<=y")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("\"abc\"<=5") shouldBe a [Failure[_]]
        }
      }

      describe(">") {
        it("should evaluate to false if left is less than right") {
          val expected = Success(false)

          val actual = interpreter.interpret("0>3")

          actual should be (expected)
        }

        it("should evaluate to false if left is equal to right") {
          val expected = Success(false)

          val actual = interpreter.interpret("3>3")

          actual should be (expected)
        }

        it("should evaluate to true if left is greater than right") {
          val expected = Success(true)

          val actual = interpreter.interpret("7>3")

          actual should be (expected)
        }

        it("should support evaluating expressions") {
          val expected = Success(true)

          val actual = interpreter.interpret("x:=7;y:=3;x>y")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("\"abc\">5") shouldBe a [Failure[_]]
        }
      }

      describe(">=") {
        it("should evaluate to false if left is less than right") {
          val expected = Success(false)

          val actual = interpreter.interpret("0>=3")

          actual should be (expected)
        }

        it("should evaluate to true if left is equal to right") {
          val expected = Success(true)

          val actual = interpreter.interpret("3>=3")

          actual should be (expected)
        }

        it("should evaluate to true if left is greater than right") {
          val expected = Success(true)

          val actual = interpreter.interpret("7>=3")

          actual should be (expected)
        }

        it("should support evaluating expressions") {
          val expected = Success(true)

          val actual = interpreter.interpret("x:=3;y:=3;x>=y")

          actual should be (expected)
        }

        it("should fail if unable to convert a value to a number") {
          interpreter.interpret("\"abc\">=5") shouldBe a [Failure[_]]
        }
      }

      describe("==") {
        it("should evaluate to true if left is equal to right (numbers)") {
          val expected = Success(true)

          val actual = interpreter.interpret("3==3")

          actual should be (expected)
        }

        it("should evaluate to true if left is equal to right (text)") {
          val expected = Success(true)

          val actual = interpreter.interpret("\"hello\"==\"hello\"")

          actual should be (expected)
        }

        it("should support evaluating expressions") {
          val expected = Success(true)

          val actual = interpreter.interpret("x:=3;y:=3;x==y")

          actual should be (expected)
        }

        it("should support comparing different types") {
          val expected = Success(false)

          val actual = interpreter.interpret("\"abc\"==5")

          actual should be (expected)
        }
      }

      describe("!=") {
        it("should evaluate to true if left is not equal to right (numbers)") {
          val expected = Success(true)

          val actual = interpreter.interpret("3!=4")

          actual should be (expected)
        }

        it("should evaluate to true if left is not equal to right (text)") {
          val expected = Success(true)

          val actual = interpreter.interpret("\"hello\"!=\"world\"")

          actual should be (expected)
        }

        it("should evaluate to false if left is equal to right") {
          val expected = Success(false)

          val actual = interpreter.interpret("3!=3")

          actual should be (expected)
        }

        it("should support evaluating expressions") {
          val expected = Success(true)

          val actual = interpreter.interpret("x:=3;y:=4;x!=y")

          actual should be (expected)
        }

        it("should support comparing different types") {
          val expected = Success(true)

          val actual = interpreter.interpret("\"abc\"!=5")

          actual should be (expected)
        }
      }
    }
  }
}
