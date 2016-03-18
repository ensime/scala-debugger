package org.scaladebugger.language.interpreters
//import acyclic.file

import scala.annotation.tailrec
import org.scaladebugger.language.models

/**
 * Created by senkwich on 2/14/16.
 */
trait DebuggerInterpreterHelpers { this: DebuggerInterpreter =>
   def fillInArgs(
    args: Seq[(models.Identifier, models.Expression)],
    argNames: Seq[models.Identifier],
    scope: models.Scope
  ): Map[models.Identifier, models.Expression] = {
    // Map args based on name, falling back to order if name not provided
    val mappedArgs = args.zipWithIndex.map { case (t, index) =>
      val (identifier, expression) = t
      (if (identifier.name.nonEmpty) identifier else argNames(index), expression)
    }.toMap

    // Fill in any missing arguments with undefined
    val missingArgs = argNames.diff(mappedArgs.keySet.toSeq)
      .map(_ -> models.Undefined).toMap
    val filledMappedArgs = mappedArgs ++ missingArgs

    filledMappedArgs.mapValues(eval(_: models.Expression, scope))
  }

   type I = models.Identifier
   type E = models.Expression
   def toEvalArgs(scope: models.Scope, l: E, r: E): Seq[(I, E)] = Seq(
    models.Identifier("l") -> eval(l, scope),
    models.Identifier("r") -> eval(r, scope)
  )

   def toBaseValue(expression: models.Expression): models.BaseValue =
    toBaseValue(expression, rootScope)

  @tailrec final def toBaseValue(
    expression: models.Expression,
    scope: models.Scope
  ): models.BaseValue = {
    expression match {
      case b: models.BaseValue => b
      case e => toBaseValue(eval(expression, scope), scope)
    }
  }

   def invokeOperator(name: String, scope: models.Scope, l: E, r: E): E =
    eval(models.FunctionCall(
      models.Identifier(name),
      toEvalArgs(scope, l, r)
    ), scope)

   def getVariable(
    name: String,
    scope: models.Scope
  ): models.Expression = scope.findVariable(models.Identifier(name)) match {
    case Some(v)  => v
    case None     => throw new RuntimeException(s"Variable $name does not exist!")
  }

   def getFunction(
    expression: models.Expression,
    scope: models.Scope
  ): models.Function = expression match {
    case f: models.Function => f
    case i: models.Identifier =>
      scope.findVariable(i) match {
        case Some(f: models.Function) => f
        case Some(x) => throw new RuntimeException(s"Function expected, but $x found!")
        case None => throw new RuntimeException(s"Function ${i.name} does not exist!")
      }
    case e => throw new RuntimeException(s"Function expected, but $e found!")
  }
}
