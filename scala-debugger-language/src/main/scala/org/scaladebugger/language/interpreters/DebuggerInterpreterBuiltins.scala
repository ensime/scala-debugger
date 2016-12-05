package org.scaladebugger.language.interpreters

import org.parboiled2.ParseError
import org.scaladebugger.language.models

import scala.util.{Failure, Success, Try}

/**
 * Contains built-in functions for the debugger interpreter.
 */
trait DebuggerInterpreterBuiltins { this: DebuggerInterpreter =>
  /** Mark parameter as required. */
  @inline private def R(name: String, doc: String = "") =
  (name, "Required" + (if (doc.nonEmpty) " ~ " + doc else ""))

  /** Mark parameter as optional. */
  @inline private def O(name: String, doc: String = "") =
  (name, "Optional" + (if (doc.nonEmpty) " ~ " + doc else ""))

  // Interpreting
  private lazy val evalCodeIdentifier = {
    val (name, doc) = R("code", "text to be parsed and evaluated")
    models.Identifier(name, Some(doc))
  }
  this.bindFunctionExpression("eval", models.NativeFunction(
    Seq(evalCodeIdentifier),
    (m, s) => toExpression(interpret(toBaseValue(m.getOrElse(
      evalCodeIdentifier,
      models.Undefined.toScalaValue
    ).asInstanceOf[models.Expression], s).toScalaValue.toString, s).get).get,
    Some("""Evaluate the text as a local code snippet.""")
  ))

  // Debugging
  private lazy val parseCodeIdentifier = {
    val (name, doc) = R("code", "text to be parsed")
    models.Identifier(name, Some(doc))
  }
  this.bindFunctionExpression("parse", models.NativeFunction(
    Seq(parseCodeIdentifier),
    (m, s) => {
      val code = toBaseValue(m.getOrElse(
        parseCodeIdentifier,
        models.Undefined
      )).toScalaValue.toString

      val results = parse(code)
      val resultString = parseResultsToString(results, code)
      toExpression(resultString).get
    },
    Some("""Parses the text, returning the AST as text.""")
  ))

  // Side effects
  this.bindFunctionWithParamDocs(
    "print", Seq(R("text")), DefaultFunctions.Print(out),
    """Prints to standard out. Invoke using print("some text")."""
  )
  this.bindFunctionWithParamDocs(
    "printErr", Seq(R("text")), DefaultFunctions.Print(err),
    """Prints to standard error. Invoke using printErr("some text")."""
  )

  // Mathematical operators
  this.bindFunctionWithParamDocs(
    "plusPlus", Seq(R("l"), R("r")), DefaultFunctions.PlusPlus,
    """Equivalent to l ++ r."""
  )
  this.bindFunctionWithParamDocs(
    "plus", Seq(R("l"), R("r")), DefaultFunctions.Plus,
    """Equivalent to l + r."""
  )
  this.bindFunctionWithParamDocs(
    "minus", Seq(R("l"), R("r")), DefaultFunctions.Minus,
    """Equivalent to l - r."""
  )
  this.bindFunctionWithParamDocs(
    "multiply", Seq(R("l"), R("r")), DefaultFunctions.Multiply,
    """Equivalent to l * r."""
  )
  this.bindFunctionWithParamDocs(
    "divide", Seq(R("l"), R("r")), DefaultFunctions.Divide,
    """Equivalent to l / r."""
  )
  this.bindFunctionWithParamDocs(
    "modulus", Seq(R("l"), R("r")), DefaultFunctions.Modulus,
    """Equivalent to l % r."""
  )

  // Logical operators
  this.bindFunctionWithParamDocs(
    "lessThan", Seq(R("l"), R("r")), DefaultFunctions.LessThan,
    """Equivalent to l < r."""
  )
  this.bindFunctionWithParamDocs(
    "lessThanEqual", Seq(R("l"), R("r")), DefaultFunctions.LessThanEqual,
    """Equivalent to l <= r."""
  )
  this.bindFunctionWithParamDocs(
    "greaterThan", Seq(R("l"), R("r")), DefaultFunctions.GreaterThan,
    """Equivalent to l > r."""
  )
  this.bindFunctionWithParamDocs(
    "greaterThanEqual", Seq(R("l"), R("r")), DefaultFunctions.GreaterThanEqual,
    """Equivalent to l >= r."""
  )
  this.bindFunctionWithParamDocs(
    "equal", Seq(R("l"), R("r")), DefaultFunctions.Equal,
    """Equivalent to l == r."""
  )
  this.bindFunctionWithParamDocs(
    "notEqual", Seq(R("l"), R("r")), DefaultFunctions.NotEqual,
    """Equivalent to l != r."""
  )

  private def parseResultsToString(results: Try[Seq[AnyRef]], input: String): String = results match {
    case Success(r)               => r.mkString(",")
    case Failure(ex: ParseError)  => ex.format(input)
    case Failure(ex)              => ex.toString
  }
}
