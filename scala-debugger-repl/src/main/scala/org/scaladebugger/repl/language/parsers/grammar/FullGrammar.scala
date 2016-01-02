package org.scaladebugger.repl.language.parsers.grammar

import org.parboiled2._
import org.scaladebugger.repl.language.models

/**
 * Represents the full grammar of the small debugger language.
 */
trait FullGrammar
  extends Parser
  with ConditionGrammar
  with FunctionGrammar
  with OperationsGrammar
  with ValueGrammar
  with WhiteSpaceGrammar
{
  def AllInputLines: Rule1[Seq[models.Expression]] = rule {
    oneOrMore(InputLine) ~ EOI
  }

  def InputLine: Rule1[models.Expression] = rule { Expression }
}
