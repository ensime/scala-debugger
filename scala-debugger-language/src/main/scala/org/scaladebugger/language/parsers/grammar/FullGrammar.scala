package org.scaladebugger.language.parsers.grammar
import acyclic.file

import org.parboiled2._
import org.scaladebugger.language.models.Expression

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
  def AllInputLines: Rule1[Seq[Expression]] = rule {
    oneOrMore(InputLine) ~ EOI
  }

  def InputLine: Rule1[Expression] = rule { Expression }
}
