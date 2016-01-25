package org.scaladebugger.language.interpreters

import org.parboiled2.ParseError
import org.scaladebugger.language.models

import scala.util.{Failure, Success, Try}

/**
 * Contains built-in functions for the debugger interpreter.
 */
trait DebuggerInterpreterBuiltins { this: DebuggerInterpreter =>
  // Interpreting
  this.bindFunctionExpression("eval", models.NativeFunction(
    Seq(models.Identifier("code")),
    (m, s) => toExpression(interpret(toBaseValue(m.getOrElse(
      models.Identifier("code"),
      models.Undefined.toScalaValue
    ).asInstanceOf[models.Expression], s).toScalaValue.toString, s).get).get,
    Some("""Evaluate the text as a local code snippet.""")
  ))

  // Debugging
  this.bindFunctionExpression("parse", models.NativeFunction(
    Seq(models.Identifier("code")),
    (m, s) => {
      val code = toBaseValue(m.getOrElse(
        models.Identifier("code"),
        models.Undefined
      )).toScalaValue.toString

      val results = parse(code)
      val resultString = parseResultsToString(results, code)
      toExpression(resultString).get
    },
    Some("""Parses the text, returning the AST as text.""")
  ))

  // Side effects
  this.bindFunction("print", Seq("text"), DefaultFunctions.Print(out),
    """Prints to standard out. Invoke using print("some text").""")
  this.bindFunction("printErr", Seq("text"), DefaultFunctions.Print(err),
    """Prints to standard error. Invoke using printErr("some text").""")

  // Mathematical operators
  this.bindFunction("plusPlus", Seq("l", "r"), DefaultFunctions.PlusPlus)
  this.bindFunction("plus", Seq("l", "r"), DefaultFunctions.Plus)
  this.bindFunction("minus", Seq("l", "r"), DefaultFunctions.Minus)
  this.bindFunction("multiply", Seq("l", "r"), DefaultFunctions.Multiply)
  this.bindFunction("divide", Seq("l", "r"), DefaultFunctions.Divide)
  this.bindFunction("modulus", Seq("l", "r"), DefaultFunctions.Modulus)

  // Logical operators
  this.bindFunction("lessThan", Seq("l", "r"), DefaultFunctions.LessThan)
  this.bindFunction("lessThanEqual", Seq("l", "r"), DefaultFunctions.LessThanEqual)
  this.bindFunction("greaterThan", Seq("l", "r"), DefaultFunctions.GreaterThan)
  this.bindFunction("greaterThanEqual", Seq("l", "r"), DefaultFunctions.GreaterThanEqual)
  this.bindFunction("equal", Seq("l", "r"), DefaultFunctions.Equal)
  this.bindFunction("notEqual", Seq("l", "r"), DefaultFunctions.NotEqual)

  private def parseResultsToString(results: Try[Seq[AnyRef]], input: String): String = results match {
    case Success(r)               => r.mkString(",")
    case Failure(ex: ParseError)  => ex.format(input)
    case Failure(ex)              => ex.toString
  }
}
