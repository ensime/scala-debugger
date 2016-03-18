package org.scaladebugger.language.parsers.grammar
//import acyclic.file

import org.parboiled2._
import org.scaladebugger.language.models

/**
 * Contains function-related grammars.
 */
trait FunctionGrammar extends Parser with ConditionGrammar
  with OperationsGrammar with ValueGrammar with WhiteSpaceGrammar
{
  val context: models.Context

  def Function: Rule1[models.IncompleteInterpretedFunction] = rule {
    ReservedKeywords.CreateFunction ~ ws('(') ~ FunctionArguments ~ ws(')') ~
    FunctionBody ~> ((i: Seq[models.Identifier], e: models.Expression) => {
      push(models.IncompleteInterpretedFunction(i, e))
    })
  }

  def FunctionArguments: Rule1[Seq[models.Identifier]] = rule {
    Identifier.*(ws(','))
  }

  def FunctionBody: Rule1[models.ExpressionGroup] = rule { ExpressionGroup }

  def FunctionCall: Rule1[models.FunctionCall] = rule {
    ((Parens | Identifier) ~ (ws('(') ~ FunctionCallCommaArguments ~ ws(')')) ~> models.FunctionCall) |
    FunctionCallUsingWhiteSpace
  }

  def FunctionCallUsingWhiteSpace: Rule1[models.FunctionCall] = rule {
    Identifier ~ WhiteSpace ~> (i => {
      val f = context.functions.find(_._1 == i).map(_._2.parameters.length)

      // TODO: Support non-base value arguments (just normal expressions like functions)
      //println(s"Checking for $f arguments for ${i.name}")
      test(f.nonEmpty) ~ push(i) ~ (
        (test(f.get <= 0) ~ push(Nil)) |
        // NOTE: Hack due to https://github.com/sirthias/parboiled2/issues/150
        //(1 to f.get).times(BaseValueFunctionArgument).separatedBy(AtLeastOneNoNewLineWhiteSpace)
        (test(f.get >= 9) ~ (1 to 9).times(BaseValueFunctionArgument).separatedBy(AtLeastOneNoNewLineWhiteSpace)) |
        (test(f.get >= 8) ~ (1 to 8).times(BaseValueFunctionArgument).separatedBy(AtLeastOneNoNewLineWhiteSpace)) |
        (test(f.get >= 7) ~ (1 to 7).times(BaseValueFunctionArgument).separatedBy(AtLeastOneNoNewLineWhiteSpace)) |
        (test(f.get >= 6) ~ (1 to 6).times(BaseValueFunctionArgument).separatedBy(AtLeastOneNoNewLineWhiteSpace)) |
        (test(f.get >= 5) ~ (1 to 5).times(BaseValueFunctionArgument).separatedBy(AtLeastOneNoNewLineWhiteSpace)) |
        (test(f.get >= 4) ~ (1 to 4).times(BaseValueFunctionArgument).separatedBy(AtLeastOneNoNewLineWhiteSpace)) |
        (test(f.get >= 3) ~ (1 to 3).times(BaseValueFunctionArgument).separatedBy(AtLeastOneNoNewLineWhiteSpace)) |
        (test(f.get >= 2) ~ (1 to 2).times(BaseValueFunctionArgument).separatedBy(AtLeastOneNoNewLineWhiteSpace)) |
        (test(f.get >= 1) ~ (1 to 1).times(BaseValueFunctionArgument).separatedBy(AtLeastOneNoNewLineWhiteSpace))
      )
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
