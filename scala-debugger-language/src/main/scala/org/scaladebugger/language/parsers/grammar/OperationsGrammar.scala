package org.scaladebugger.language.parsers.grammar
//import acyclic.file

import org.parboiled2._
import org.scaladebugger.language
import org.scaladebugger.language.models._

/**
 * Contains operation-related grammars.
 */
trait OperationsGrammar extends Parser with ValueGrammar with WhiteSpaceGrammar {
  this: FunctionGrammar with ConditionGrammar =>

  def Parens: Rule1[Expression] = rule {
    ws('(') ~ Expression ~ ws(')')
  }

  def Assignment: Rule1[Variable] = rule {
    Identifier ~ ws(":=") ~ Expression ~> language.models.Variable
  }

  def ExpressionGroup: Rule1[ExpressionGroup] = rule {
    ws('{') ~ zeroOrMore(Expression) ~ ws('}') ~> language.models.ExpressionGroup
  }

  def SkipEval: Rule1[SkipEval] = rule {
    ws(ReservedKeywords.SkipEval) ~ (
      FunctionCall | ExpressionGroup | Parens | Identifier
    ) ~> language.models.SkipEval
  }

  def BaseValue: Rule1[Expression] = rule {
    SkipEval | Parens | ExpressionGroup | Number | Text |
    Truthy | Falsey | Undefined | Identifier
  }

  def Factor: Rule1[Expression] = rule {
    Assignment | Conditional | Function | FunctionCall | BaseValue
  }

  def Term: Rule1[Expression] = rule {
    Factor ~ zeroOrMore(
      ws('*') ~ Factor ~> language.models.Multiply |
      ws('/') ~ Factor ~> language.models.Divide   |
      ws('%') ~ Factor ~> language.models.Modulus
    )
  }

  def Equation: Rule1[Expression] = rule {
    Term ~ zeroOrMore(
      ws("++") ~ Term ~> PlusPlus |
      ws('+') ~ Term ~> Plus |
      ws('-') ~ Term ~> language.models.Minus
    )
  }

  def Expression: Rule1[Expression] = rule {
    WhiteSpace ~ Equation ~ zeroOrMore(
      ws('<')   ~ Equation ~> language.models.Less         |
      ws("<=")  ~ Equation ~> language.models.LessEqual    |
      ws('>')   ~ Equation ~> language.models.Greater      |
      ws(">=")  ~ Equation ~> language.models.GreaterEqual |
      ws("==")  ~ Equation ~> language.models.Equal        |
      ws("!=")  ~ Equation ~> language.models.NotEqual
    ) ~ WhiteSpace ~ optional(';' ~ WhiteSpace)
  }
}
