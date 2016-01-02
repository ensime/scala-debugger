package org.scaladebugger.repl.language.parsers.grammar

import org.parboiled2._
import org.scaladebugger.repl.language.models

/**
 * Contains operation-related grammars.
 */
trait ConditionGrammar extends Parser with OperationsGrammar with WhiteSpaceGrammar {
  this: FunctionGrammar =>
  import ReservedKeywords._

  def Conditional: Rule1[models.Conditional] = rule {
    If ~ ws('(') ~ Expression ~ ws(')') ~ Expression ~ optional(Else ~ Expression) ~>
      ((c: models.Expression, i: models.Expression, e: Option[models.Expression]) =>
        models.Conditional(c, i, e.getOrElse(models.NoOp())))
  }
}
