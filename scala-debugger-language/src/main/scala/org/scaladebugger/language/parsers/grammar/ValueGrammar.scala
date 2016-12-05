package org.scaladebugger.language.parsers.grammar

import org.parboiled2._
import org.scaladebugger.language
import org.scaladebugger.language.models

/**
 * Contains lowest-level, value-based grammars.
 */
trait ValueGrammar extends Parser with WhiteSpaceGrammar {
  def Digits: Rule0 = rule { oneOrMore(CharPredicate.Digit) }

  def Truthy: Rule1[models.Truth] = rule {
    ReservedKeywords.Truthy ~ WhiteSpace ~ push(models.Truth(value = true))
  }

  def Falsey: Rule1[models.Truth] = rule {
    ReservedKeywords.Falsey ~ WhiteSpace ~ push(models.Truth(value = false))
  }

  def Undefined: Rule1[models.BaseValue] = rule {
    ReservedKeywords.Undefined ~ WhiteSpace ~ push(models.Undefined)
  }

  def Number: Rule1[models.Number] = rule {
    capture(optional('-') ~ ((optional(Digits) ~ '.' ~ Digits) | Digits)) ~>
      ((n: String) => models.Number(n.toDouble))
  }

  def Identifier: Rule1[models.Identifier] = rule {
    capture(CharPredicate.Alpha ~ zeroOrMore(CharPredicate.AlphaNum)) ~
      optional(ws(':') ~ Text) ~> ((i: String, d: Option[models.Text]) =>
        test(!ReservedKeywords.All.contains(i)) ~ push(models.Identifier(i, d.map(_.value))))
  }

  def TextChar: Rule0 = rule { CharPredicate.Visible ++ WhiteSpaceChar -- '"' }

  def Text: Rule1[models.Text] = rule {
    '"' ~ capture(zeroOrMore(TextChar)) ~ '"' ~> models.Text
  }
}
