package org.scaladebugger.repl.language.parsers.grammar

import org.parboiled2._
import org.scaladebugger.repl.language.models

/**
 * Contains operation-related grammars.
 */
trait OperationsGrammar extends Parser with ValueGrammar with WhiteSpaceGrammar {
  this: FunctionGrammar with ConditionGrammar =>

  def Parens: Rule1[models.Expression] = rule {
    ws('(') ~ Expression ~ ws(')')
  }

  def Assignment: Rule1[models.Variable] = rule {
    Identifier ~ ws(":=") ~ Expression ~> models.Variable
  }

  def ExpressionGroup: Rule1[models.ExpressionGroup] = rule {
    ws('{') ~ zeroOrMore(Expression) ~ ws('}') ~> models.ExpressionGroup
  }

  def BaseValue: Rule1[models.Expression] = rule {
    Parens | ExpressionGroup | Number | Text | Truthy | Falsey | Identifier
  }

  def Factor: Rule1[models.Expression] = rule {
    Assignment | Conditional | Function | FunctionCall | BaseValue
  }

  def Term: Rule1[models.Expression] = rule {
    Factor ~ zeroOrMore(
      ws('*') ~ Factor ~> models.Multiply |
      ws('/') ~ Factor ~> models.Divide   |
      ws('%') ~ Factor ~> models.Modulus
    )
  }

  def Equation: Rule1[models.Expression] = rule {
    Term ~ zeroOrMore(
      ws("++") ~ Term ~> models.PlusPlus |
      ws('+') ~ Term ~> models.Plus |
      ws('-') ~ Term ~> models.Minus
    )
  }

  def Expression: Rule1[models.Expression] = rule {
    WhiteSpace ~ Equation ~ zeroOrMore(
      ws('<')   ~ Equation ~> models.Less         |
      ws("<=")  ~ Equation ~> models.LessEqual    |
      ws('>')   ~ Equation ~> models.Greater      |
      ws(">=")  ~ Equation ~> models.GreaterEqual |
      ws("==")  ~ Equation ~> models.Equal        |
      ws("!=")  ~ Equation ~> models.NotEqual
    ) ~ WhiteSpace ~ optional(';' ~ WhiteSpace)
  }
}
