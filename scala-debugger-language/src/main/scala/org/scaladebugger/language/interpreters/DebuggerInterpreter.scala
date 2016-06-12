package org.scaladebugger.language.interpreters
//import acyclic.file

import org.parboiled2._
import org.scaladebugger.language.models
import org.scaladebugger.language.parsers.DebuggerParser

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

class DebuggerInterpreter(
  protected val rootScope: models.Scope = models.Scope.newRootScope(),
  protected val out: java.io.PrintStream = Console.out,
  protected val err: java.io.PrintStream = Console.err
) extends Interpreter with DebuggerInterpreterBuiltins with DebuggerInterpreterHelpers {
  /** Represents the context information about the interpreter state. */
  override lazy val context: models.Context = models.Context(rootScope)

  override def put(name: String, value: Any): Unit = {
    val baseValue = toExpression(value).get
    putExpression(name, baseValue)
  }

  def putExpression(name: String, value: models.Expression): Unit = {
    rootScope.variables.put(models.Identifier(name), value)
  }

  override def get(name: String): Option[Any] = {
    getExpression(name)
      .map(toBaseValue)
      .map(_.toScalaValue)
  }

  def getExpression(name: String): Option[models.Expression] = {
    Try(getVariable(name, rootScope)).toOption
  }

  def bindFunction(
    name: String,
    parameters: Seq[String],
    function: (Map[String, Any]) => Any,
    documentation: String = null
  ): Unit = {
    val functionExpression = models.NativeFunction(
      parameters.map(models.Identifier.apply),
      (m: Map[models.Identifier, models.Expression], _) => toExpression(
        function(m.filter {
          // TODO: Support filtering for native functions so you don't have
          //       variables with an undefined model as the value
          case (i, e) => true//e != models.Undefined
        }.map { case (i, e) => (i.name, toPrimitiveValue(e).get) })
      ).get,
      Option(documentation)
    )
    bindFunctionExpression(name, functionExpression)
  }

  def bindFunctionExpression(name: String, function: models.Function): Unit = {
    rootScope.variables.put(models.Identifier(name), function)
  }

  private def toPrimitiveValue(expression: models.Expression): Try[Any] = Try(expression match {
    case p: models.Primitive => p.value
    case _ => throw new RuntimeException(s"Unable to convert $expression to primitive value!")
  })

  protected def toExpression(value: Any): Try[models.Expression] = {
    Try(value.toString.toDouble).map(models.Number.apply) orElse
    Try(value.toString.toBoolean).map(models.Truth.apply) orElse
    Try(value.asInstanceOf[Unit]).map(_ => models.Undefined) orElse
    Try(value.toString).map(models.Text.apply) orElse
    Failure(new RuntimeException(s"Unable to convert $value to expression!"))
  }

  /** Interprets code and returns collection of results for all top-level expressions */
  def interpretVerbosely(code: String): Try[Seq[Try[AnyRef]]] = {
    interpretVerbosely(code, rootScope)
  }

  private def interpretVerbosely(code: String, scope: models.Scope): Try[Seq[Try[AnyRef]]] = {
    process(parse(code), scope)
  }

  protected def parse(code: String): Try[Seq[models.Expression]] =
    new DebuggerParser(code, models.Context(rootScope)).AllInputLines.run()

  private def process(
    results: Try[Seq[models.Expression]],
    scope: models.Scope
  ): Try[Seq[Try[AnyRef]]] = {
    results match {
      case Success(astCollection) =>
        val results = astCollection.map(ast => Try(eval(ast, scope)))
        Success(results.map(_.map(toBaseValue).flatMap(r => Try(r.toScalaValue))))
      case Failure(ex: ParseError)  => Failure(ex)
      case Failure(e)               => Failure(e)
    }
  }

  /** Interprets code and returns result of last top-level expression */
  override def interpret(code: String): Try[AnyRef] = interpret(code, rootScope)

  protected def interpret(code: String, scope: models.Scope): Try[AnyRef] = interpretVerbosely(code, scope) match {
    case Success(results) => results.last
    case Failure(ex)      => Failure(ex)
  }

  def eval(expression: models.Expression): models.Expression =
    eval(expression, rootScope)

  final protected def eval(
    expression: models.Expression,
    scope: models.Scope
  ): models.Expression = expression match {
    case models.IncompleteInterpretedFunction(p, b, d) =>
      models.InterpretedFunction(p, scope, b, d)
    case v: models.BaseValue => v
    case models.SkipEval(e) => e
    case models.Identifier(name) =>
      val e = eval(getVariable(name, scope), scope)

      // TODO: Avoid hack where parser treats function call as variable in
      //       "x:func(){};x"
      e match {
        case f: models.Function =>
          eval(models.FunctionCall(models.Identifier(name), Nil), scope)
        case _ => e
      }
    case models.ExpressionGroup(exprs) =>
      val newScope = models.Scope.newChildScope(scope)
      exprs.take(exprs.length - 1).foreach(eval(_: models.Expression, newScope))
      eval(exprs.lastOption.getOrElse(models.Undefined), newScope)
    case models.Conditional(c, t, f) =>
      if (eval(c, scope) == models.Truth(value = true)) eval(t, scope)
      else eval(f, scope)
    case models.Variable(i, e) =>
      // Look for a scope with the variable, otherwise use the current scope
      @tailrec def findScopeForVariable(
        identifier: models.Identifier,
        scope: models.Scope
      ): Option[models.Scope] = {
        val isInScope = scope.variables.contains(identifier)

        if (isInScope) Some(scope)
        else if (scope.parent.nonEmpty) findScopeForVariable(identifier, scope.parent.get)
        else None
      }

      val vScope = findScopeForVariable(i, scope).getOrElse(scope)
      vScope.variables.put(i, eval(e, scope))
      eval(getVariable(i.name, vScope), scope)
    case models.FunctionCall(e, args) => getFunction(e, scope) match {
      case models.IncompleteInterpretedFunction(p, b, d) =>
        val f = models.InterpretedFunction(p, scope, b, d)
        val fCall = models.FunctionCall(f, args)
        eval(fCall, scope)
      case models.InterpretedFunction(p, c, b, _) =>
        val evaluatedMappedArgs = fillInArgs(args, p, scope)
        val argScope = models.Scope.newChildScope(c, evaluatedMappedArgs)
        eval(b, argScope)
      case models.NativeFunction(p, f, _) =>
        val evaluatedMappedArgs = fillInArgs(args, p, scope)
        eval(f(evaluatedMappedArgs, scope), scope)
    }
    case models.PlusPlus(l, r) => invokeOperator("plusPlus", scope, l, r)
    case models.Plus(l, r) => invokeOperator("plus", scope, l, r)
    case models.Minus(l, r) => invokeOperator("minus", scope, l, r)
    case models.Multiply(l, r) => invokeOperator("multiply", scope, l, r)
    case models.Divide(l, r) => invokeOperator("divide", scope, l, r)
    case models.Modulus(l, r) => invokeOperator("modulus", scope, l, r)
    case models.Less(l, r) => invokeOperator("lessThan", scope, l, r)
    case models.LessEqual(l, r) => invokeOperator("lessThanEqual", scope, l, r)
    case models.Greater(l, r) => invokeOperator("greaterThan", scope, l, r)
    case models.GreaterEqual(l, r) => invokeOperator("greaterThanEqual", scope, l, r)
    case models.Equal(l, r) => invokeOperator("equal", scope, l, r)
    case models.NotEqual(l, r) => invokeOperator("notEqual", scope, l, r)
  }
}
