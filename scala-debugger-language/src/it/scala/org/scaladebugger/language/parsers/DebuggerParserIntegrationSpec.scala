package org.scaladebugger.language.parsers

import org.parboiled2.{ErrorFormatter, ParseError}
import org.scaladebugger.language.models._
import org.scaladebugger.language.parsers.grammar.ReservedKeywords
import org.scalatest.{Matchers, ParallelTestExecution, FunSpec}

import scala.util.{Failure, Success}

class DebuggerParserIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution
{
  private def parse(code: String, context: Context = Context.blank) = {
    new DebuggerParser(code, context).AllInputLines.run()
  }

  private def parseAllAsExpressions(code: String, context: Context = Context.blank): Seq[Expression] = {
    parse(code, context) match {
      case Success(ast)           => ast
      case Failure(e: ParseError) =>
        throw new RuntimeException(e.format(code, new ErrorFormatter(showTraces = true)))
      case Failure(e)             => throw e
    }
  }

  private def parseFirstAsExpression(code: String, context: Context = Context.blank): Expression = {
    parseAllAsExpressions(code, context).head
  }

  describe("DebuggerParser") {
    describe("functions") {
      they("should support parsing definitions") {
        val f = ReservedKeywords.CreateFunction

        parseFirstAsExpression(s"$f(){}") should be (IncompleteInterpretedFunction(Nil, ExpressionGroup(Nil)))
        parseFirstAsExpression(s"$f() {}") should be (IncompleteInterpretedFunction(Nil, ExpressionGroup(Nil)))
        parseFirstAsExpression(s"$f (){}") should be (IncompleteInterpretedFunction(Nil, ExpressionGroup(Nil)))
        parseFirstAsExpression(s"$f () {}") should be (IncompleteInterpretedFunction(Nil, ExpressionGroup(Nil)))
        parseFirstAsExpression(s"$f(a){}") should be (IncompleteInterpretedFunction(Seq(Identifier("a")), ExpressionGroup(Nil)))
        parseFirstAsExpression(s"$f( a ){}") should be (IncompleteInterpretedFunction(Seq(Identifier("a")), ExpressionGroup(Nil)))
        parseFirstAsExpression(s"$f(a,b){}") should be (IncompleteInterpretedFunction(Seq(Identifier("a"), Identifier("b")), ExpressionGroup(Nil)))
        parseFirstAsExpression(s"$f( a , b ){}") should be (IncompleteInterpretedFunction(Seq(Identifier("a"), Identifier("b")), ExpressionGroup(Nil)))
        parseFirstAsExpression(s"$f(){a;b}") should be (IncompleteInterpretedFunction(Nil, ExpressionGroup(Seq(Identifier("a"), Identifier("b")))))
      }

      they("should support parsing invocations using parentheses and commas") {
        val f = "someVariableAsAFunction"

        parseFirstAsExpression(s"$f()") should be (FunctionCall(Identifier(f), Nil))
        parseFirstAsExpression(s"$f(a)") should be (FunctionCall(Identifier(f), Seq(Identifier("") -> Identifier("a"))))
        parseFirstAsExpression(s"$f(arg=a)") should be (FunctionCall(Identifier(f), Seq(Identifier("arg") -> Identifier("a"))))
        parseFirstAsExpression(s"$f( arg = a )") should be (FunctionCall(Identifier(f), Seq(Identifier("arg") -> Identifier("a"))))
        parseFirstAsExpression(s"$f(arg1=a,b,arg2=c)") should be (FunctionCall(Identifier(f), Seq(
          Identifier("arg1") -> Identifier("a"),
          Identifier("") -> Identifier("b"),
          Identifier("arg2") -> Identifier("c")
        )))
        parseFirstAsExpression(s"$f( arg1 = a , b , arg2 = c )") should be (FunctionCall(Identifier(f), Seq(
          Identifier("arg1") -> Identifier("a"),
          Identifier("") -> Identifier("b"),
          Identifier("arg2") -> Identifier("c")
        )))

        // Verify function returned in parens can be parsed as invocation
        val fDef = ReservedKeywords.CreateFunction + "(){}"
        parseFirstAsExpression(s"($fDef)()") should be (FunctionCall(
          IncompleteInterpretedFunction(Nil, ExpressionGroup(Nil)),
          Nil
        ))
      }

      they("should support parsing invocations using only whitespace") {
        val f = "someVariableAsAFunction"

        val context0 = Context(Scope.newRootScope(Map(
          Identifier(f) -> IncompleteInterpretedFunction(Nil, null)
        )))
        parseFirstAsExpression(s"$f", context0) should be (FunctionCall(Identifier(f), Nil))

        val context1 = Context(Scope.newRootScope(Map(
          Identifier(f) -> IncompleteInterpretedFunction(Seq(Identifier("arg")), null)
        )))
        parseFirstAsExpression(s"$f a", context1) should be (FunctionCall(Identifier(f), Seq(Identifier("") -> Identifier("a"))))
        parseFirstAsExpression(s"$f arg=a", context1) should be (FunctionCall(Identifier(f), Seq(Identifier("arg") -> Identifier("a"))))
        parseFirstAsExpression(s"$f arg = a", context1) should be (FunctionCall(Identifier(f), Seq(Identifier("arg") -> Identifier("a"))))

        val context2 = Context(Scope.newRootScope(Map(
          Identifier(f) -> IncompleteInterpretedFunction(Seq(Identifier("arg"), Identifier("")), null)
        )))
        parseFirstAsExpression(s"$f a b", context2) should be (FunctionCall(Identifier(f), Seq(
          Identifier("") -> Identifier("a"),
          Identifier("") -> Identifier("b")
        )))
        parseFirstAsExpression(s"$f arg = a b", context2) should be (FunctionCall(Identifier(f), Seq(
          Identifier("arg") -> Identifier("a"),
          Identifier("") -> Identifier("b")
        )))

        val context3 = Context(Scope.newRootScope(Map(
          Identifier(f) -> IncompleteInterpretedFunction(Seq(Identifier(""), Identifier("arg")), null)
        )))
        parseFirstAsExpression(s"$f a arg = b", context3) should be (FunctionCall(Identifier(f), Seq(
          Identifier("") -> Identifier("a"),
          Identifier("arg") -> Identifier("b")
        )))
      }

      they("should support parsing assignment of definitions to variables") {
        val f = ReservedKeywords.CreateFunction
        val n = "someVariableName"

        parseFirstAsExpression(s"$n:=$f(){}") should be (Variable(Identifier(n), IncompleteInterpretedFunction(Nil, ExpressionGroup(Nil))))
        parseFirstAsExpression(s"$n := $f () {}") should be (Variable(Identifier(n), IncompleteInterpretedFunction(Nil, ExpressionGroup(Nil))))
        parseFirstAsExpression(s"$n := $f ( ) { }") should be (Variable(Identifier(n), IncompleteInterpretedFunction(Nil, ExpressionGroup(Nil))))
      }
    }

    describe("operations") {
      they("should support parsing skip evaluation expressions") {
        parseFirstAsExpression("@x") should be (SkipEval(Identifier("x")))
        parseFirstAsExpression("@ x") should be (SkipEval(Identifier("x")))
        parseFirstAsExpression("@(3)") should be (SkipEval(Number(3)))
        parseFirstAsExpression("@{1;2;3}") should be (SkipEval(ExpressionGroup(Seq(
          Number(1), Number(2), Number(3)
        ))))
        parseFirstAsExpression("@x()") should be (SkipEval(FunctionCall(Identifier("x"), Nil)))
      }

      they("should support parsing parenthesis as another expression") {
        parseFirstAsExpression("(0)") should be (Number(0))
        parseFirstAsExpression(" (0)") should be (Number(0))
        parseFirstAsExpression("(0) ") should be (Number(0))
        parseFirstAsExpression("( 0)") should be (Number(0))
        parseFirstAsExpression("(0 )") should be (Number(0))
        parseFirstAsExpression("( 0 )") should be (Number(0))
        parseFirstAsExpression(" (0) ") should be (Number(0))
      }

      they("should support parsing an assignment") {
        parseFirstAsExpression("x:=0") should be (Variable(Identifier("x"), Number(0)))
        parseFirstAsExpression(" x:=0") should be (Variable(Identifier("x"), Number(0)))
        parseFirstAsExpression("x:=0 ") should be (Variable(Identifier("x"), Number(0)))
        parseFirstAsExpression("x :=0") should be (Variable(Identifier("x"), Number(0)))
        parseFirstAsExpression("x:= 0") should be (Variable(Identifier("x"), Number(0)))
        parseFirstAsExpression("x := 0") should be (Variable(Identifier("x"), Number(0)))
        parseFirstAsExpression(" x := 0") should be (Variable(Identifier("x"), Number(0)))
        parseFirstAsExpression(" x := 0 ") should be (Variable(Identifier("x"), Number(0)))
      }

      they("should support being separated by semicolons") {
        parseAllAsExpressions("x:=0;y:=0;z:=0") should be (Seq(
          Variable(Identifier("x"), Number(0)),
          Variable(Identifier("y"), Number(0)),
          Variable(Identifier("z"), Number(0))
        ))
      }

      they("should support parsing groups of expressions") {
        parseFirstAsExpression("{}") should be (ExpressionGroup(Nil))
        parseFirstAsExpression("{a}") should be (ExpressionGroup(Seq(Identifier("a"))))
        parseFirstAsExpression("{a;b}") should be (ExpressionGroup(Seq(Identifier("a"), Identifier("b"))))
        parseFirstAsExpression(
          """
            |{
            |a
            |b
            |c
            |}
          """.stripMargin) should be (ExpressionGroup(Seq(
          Identifier("a"),
          Identifier("b"),
          Identifier("c")
        )))
      }

      they("should support parsing logic operations from left to right") {
        val expected = NotEqual(
          Equal(
            GreaterEqual(
              Greater(
                LessEqual(
                  Less(
                    Number(0),
                    Number(1)
                  ),
                  Number(2)
                ),
                Number(3)
              ),
              Number(4)
            ),
            Number(5)
          ),
          Number(6)
        )

        parseFirstAsExpression("0<1<=2>3>=4==5!=6") should be (expected)
        parseFirstAsExpression("0 < 1 <= 2 > 3 >= 4 == 5 != 6") should be (expected)
      }

      they("should support parsing plus/minus operations from left to right") {
        val expected = PlusPlus(
          Minus(
            Plus(
              Number(0),
              Number(1)
            ),
            Number(2)
          ),
          Number(3)
        )

        parseFirstAsExpression("0+1-2++3") should be (expected)
        parseFirstAsExpression("0 + 1 - 2 ++ 3") should be (expected)
      }

      they("should support parsing division/modulus/multiplication from left to right") {
        val expected = Modulus(
          Divide(
            Multiply(
              Number(0),
              Number(1)
            ),
            Number(2)
          ),
          Number(3)
        )

        parseFirstAsExpression("0*1/2%3") should be (expected)
        parseFirstAsExpression("0 * 1 / 2 % 3") should be (expected)
      }

      they("should support parsing following an expected tree of Logic > Plus/Minus > Divide/Multiply > Everything Else") {
        val i = ReservedKeywords.If
        val e = ReservedKeywords.Else
        val t = ReservedKeywords.Truthy
        val f = ReservedKeywords.Falsey

        // Check order (Number representing Everything Else)
        parseFirstAsExpression("0>1+2*3") should be (Greater(Number(0), Plus(Number(1), Multiply(Number(2), Number(3)))))
        parseFirstAsExpression("((0>1)+2)*3") should be (Multiply(Plus(Greater(Number(0), Number(1)), Number(2)), Number(3)))
        parseFirstAsExpression("{{0>1}+2}*3") should be (Multiply(
          ExpressionGroup(Seq(
            Plus(ExpressionGroup(Seq(
              Greater(Number(0), Number(1))
            )), Number(2))
          )),
          Number(3)
        ))

        // Check operations can be used against an assignment
        parseFirstAsExpression("0*a:=1") should be (Multiply(Number(0), Variable(Identifier("a"), Number(1))))
        parseFirstAsExpression("a:=1*0") should be (Variable(Identifier("a"), Multiply(Number(1), Number(0))))
        parseFirstAsExpression("0 * a := 1") should be (Multiply(Number(0), Variable(Identifier("a"), Number(1))))
        parseFirstAsExpression("a := 1 * 0") should be (Variable(Identifier("a"), Multiply(Number(1), Number(0))))

        // Check operations can be used against conditional
        parseFirstAsExpression(s"0*$i(a)a $e b") should be (Multiply(
          Number(0),
          Conditional(
            Identifier("a"),
            Identifier("a"),
            Identifier("b")
          )
        ))
        parseFirstAsExpression(s"$i(a)a $e b*0") should be (Conditional(
          Identifier("a"),
          Identifier("a"),
          Multiply(Identifier("b"), Number(0))
        ))
        parseFirstAsExpression(s"0*$i(a){a}$e{b}") should be (Multiply(
          Number(0),
          Conditional(
            Identifier("a"),
            ExpressionGroup(Seq(Identifier("a"))),
            ExpressionGroup(Seq(Identifier("b")))
          )
        ))
        parseFirstAsExpression(s"$i(a){a}$e{b}*0") should be (Conditional(
          Identifier("a"),
          ExpressionGroup(Seq(Identifier("a"))),
          Multiply(ExpressionGroup(Seq(Identifier("b"))), Number(0))
        ))

        // Check other types of Everything Else are below multiply/divide/...
        parseFirstAsExpression(s"""(z:=0)*($i(a){a}$e{b})*func(){}*c()*(0)*{1}*2*"text"*$t*$f*aaa""") should be (
          Multiply(
            Multiply(
              Multiply(
                Multiply(
                  Multiply(
                    Multiply(
                      Multiply(
                        Multiply(
                          Multiply(
                            Multiply(
                              Variable(
                                Identifier("z"),
                                Number(0)
                              ),
                              Conditional(
                                Identifier("a"),
                                ExpressionGroup(Seq(Identifier("a"))),
                                ExpressionGroup(Seq(Identifier("b"))
                              ))
                            ),
                            IncompleteInterpretedFunction(Nil, ExpressionGroup(Nil))
                          ),
                          FunctionCall(Identifier("c"), Nil)
                        ),
                        Number(0)
                      ),
                      ExpressionGroup(Seq(Number(1)))
                    ),
                    Number(2)
                  ),
                  Text("text")
                ),
                Truth(value = true)
              ),
              Truth(value = false)
            ),
            Identifier("aaa")
          )
        )
      }
    }

    describe("conditionals") {
      they("should support parsing if statements") {
        val i = ReservedKeywords.If

        parseFirstAsExpression(s"$i(a)a") should be (Conditional(Identifier("a"), Identifier("a"), Undefined))
        parseFirstAsExpression(s"$i(a){a}") should be (Conditional(Identifier("a"), ExpressionGroup(Seq(Identifier("a"))), Undefined))
      }

      they("should support parsing else clauses") {
        val i = ReservedKeywords.If
        val e = ReservedKeywords.Else

        parseFirstAsExpression(s"$i(a)a $e b") should be (Conditional(Identifier("a"), Identifier("a"), Identifier("b")))
        parseFirstAsExpression(s"$i(a){a}$e{b}") should be (Conditional(Identifier("a"), ExpressionGroup(Seq(Identifier("a"))), ExpressionGroup(Seq(Identifier("b")))))
      }
    }

    describe("identifiers") {
      they("should support parsing names") {
        val expected = Identifier("notAKeyword")

        val actual = parseFirstAsExpression(expected.name)

        actual should be (expected)
      }

      they("should not use keywords as identifiers") {
        ReservedKeywords.All.foreach { keyword =>
          val result = parse(keyword)
          result match {
            case Success(_: Identifier) =>
              fail(s"Unexpected identifier from $keyword")
            case Success(e) =>
              e should not be an [Identifier]
            case Failure(ex) =>
              ex shouldBe a [ParseError]
          }
        }
      }
    }

    describe("primitives") {
      they("should support parsing numbers") {
        parseFirstAsExpression("0.0")  should be (Number(0.0))
        parseFirstAsExpression("1.0")  should be (Number(1.0))
        parseFirstAsExpression("-1.0") should be (Number(-1.0))
        parseFirstAsExpression("0.2")  should be (Number(0.2))
        parseFirstAsExpression("-0.2") should be (Number(-0.2))
        parseFirstAsExpression(".2")   should be (Number(.2))
        parseFirstAsExpression("-.2")  should be (Number(-.2))
      }

      they("should support parsing booleans") {
        parseFirstAsExpression("false")  should be (Truth(value = false))
        parseFirstAsExpression("true")   should be (Truth(value = true))
      }

      they("should support undefined") {
        parseFirstAsExpression("undefined") should be (Undefined)
      }

      they("should support parsing strings") {
        parseFirstAsExpression("\"\"") should be (Text(""))
        parseFirstAsExpression("\" \n\r\t\f\"") should be (Text(" \n\r\t\f"))
        parseFirstAsExpression("\"sometext\"") should be (Text("sometext"))
        parseFirstAsExpression("\"some text\"") should be (Text("some text"))
        parseFirstAsExpression("\"some\ntext\"") should be (Text("some\ntext"))
        parseFirstAsExpression("\"some\rtext\"") should be (Text("some\rtext"))
        parseFirstAsExpression("\"some\ttext\"") should be (Text("some\ttext"))
        parseFirstAsExpression("\"some\ftext\"") should be (Text("some\ftext"))
      }
    }
  }
}
