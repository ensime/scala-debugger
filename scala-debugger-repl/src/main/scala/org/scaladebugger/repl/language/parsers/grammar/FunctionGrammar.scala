package org.scaladebugger.repl.language.parsers.grammar

import org.parboiled2._
import org.scaladebugger.repl.language.models

/**
 * Contains function-related grammars.
 */
trait FunctionGrammar extends Parser with ConditionGrammar
  with OperationsGrammar with ValueGrammar with WhiteSpaceGrammar
{
  val context: models.Context

  def Function: Rule1[models.InterpretedFunction] = rule {
    ReservedKeywords.CreateFunction ~ ws('(') ~ FunctionArguments ~ ws(')') ~
      FunctionBody ~> models.InterpretedFunction
  }

  def FunctionArguments: Rule1[Seq[models.Identifier]] = rule {
    Identifier.*(ws(','))
  }

  def FunctionBody: Rule1[models.ExpressionGroup] = rule { ExpressionGroup }

  def FunctionCall: Rule1[models.FunctionCall] = rule {
    (Identifier ~ (ws('(') ~ FunctionCallCommaArguments ~ ws(')')) ~> models.FunctionCall) |
    FunctionCallUsingWhiteSpace
  }

  def FunctionCallUsingWhiteSpace: Rule1[models.FunctionCall] = rule {
    Identifier ~ AtLeastOneNoNewLineWhiteSpace ~> (i => {
      val f = context.functions.find(_._1 == i).map(_._2.parameters.length)

      // TODO: Support non-base value arguments (just normal expressions like functions)
      //println(s"Checking for $f arguments for ${i.name}")
      test(f.nonEmpty) ~ push(i)~ f.get.times(BaseValueFunctionArgument).separatedBy(AtLeastOneNoNewLineWhiteSpace)
    }) ~> models.FunctionCall
  }

  def FunctionCallCommaArguments: Rule1[Seq[(models.Identifier, models.Expression)]] = rule {
    FunctionArgument.*(ws(','))
  }

  def FunctionArgument: Rule1[(models.Identifier, models.Expression)] = rule {
    optional(FunctionArgumentName) ~ Expression ~>
      ((i: Option[models.Identifier], e: models.Expression) => (i.getOrElse(models.Identifier("")), e))
  }

  def BaseValueFunctionArgument: Rule1[(models.Identifier, models.Expression)] = rule {
    optional(FunctionArgumentName) ~ BaseValue ~>
      ((i: Option[models.Identifier], e: models.Expression) => (i.getOrElse(models.Identifier("")), e))
  }

  def FunctionArgumentName: Rule1[models.Identifier] = rule {
    Identifier ~ ws('=')
  }
}
