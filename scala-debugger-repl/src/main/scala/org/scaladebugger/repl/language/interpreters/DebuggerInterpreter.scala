package org.scaladebugger.repl.language.interpreters

import org.parboiled2._
import org.scaladebugger.repl.language.parsers.DebuggerParser
import org.scaladebugger.repl.language.models

import scala.annotation.tailrec
import scala.util.{Try, Failure, Success}

class DebuggerInterpreter(
  private val rootScope: models.Scope = models.Scope.newRootScope(),
  private val out: java.io.PrintStream = Console.out,
  private val err: java.io.PrintStream = Console.err
) extends Interpreter {

  this.bindFunction("print", Seq("text"), DefaultFunctions.Print(out))
  this.bindFunction("printErr", Seq("text"), DefaultFunctions.Print(err))

  this.bindFunction("plusPlus", Seq("l", "r"), DefaultFunctions.PlusPlus)
  this.bindFunction("plus", Seq("l", "r"), DefaultFunctions.Plus)
  this.bindFunction("minus", Seq("l", "r"), DefaultFunctions.Minus)
  this.bindFunction("multiply", Seq("l", "r"), DefaultFunctions.Multiply)
  this.bindFunction("divide", Seq("l", "r"), DefaultFunctions.Divide)
  this.bindFunction("modulus", Seq("l", "r"), DefaultFunctions.Modulus)

  this.bindFunction("lessThan", Seq("l", "r"), DefaultFunctions.LessThan)
  this.bindFunction("lessThanEqual", Seq("l", "r"), DefaultFunctions.LessThanEqual)
  this.bindFunction("greaterThan", Seq("l", "r"), DefaultFunctions.GreaterThan)
  this.bindFunction("greaterThanEqual", Seq("l", "r"), DefaultFunctions.GreaterThanEqual)
  this.bindFunction("equal", Seq("l", "r"), DefaultFunctions.Equal)
  this.bindFunction("notEqual", Seq("l", "r"), DefaultFunctions.NotEqual)

  def repl(): Unit = replImpl()

  def put(name: String, value: Any): Unit = {
    val baseValue = toExpression(value).get
    rootScope.variables.put(models.Identifier(name), baseValue)
  }

  def get(name: String): Option[Any] = {
    Try(getVariable(name, rootScope)).toOption
  }

  def bindFunction(name: String, parameters: Seq[String], function: (Map[String, Any]) => Any): Unit = {
    val functionExpression = models.NativeFunction(
      parameters.map(models.Identifier.apply),
      (m: Map[models.Identifier, models.Expression]) => toExpression(
        function(m.map { case (i, e) => (i.name, toValue(e).get) })
      ).get
    )
    rootScope.variables.put(models.Identifier(name), functionExpression)
  }

  private def toValue(expression: models.Expression): Try[Any] = Try(expression match {
    case p: models.Primitive => p.value
    case models.NativeFunction(_, f) => f
    case _ => throw new RuntimeException(s"Unable to convert $expression to value!")
  })

  private def toExpression(value: Any): Try[models.Expression] = {
    Try(value.toString.toDouble).map(models.Number.apply) orElse
    Try(value.toString.toBoolean).map(models.Truth.apply) orElse
    Try(value.toString).map(models.Text.apply) orElse
    Failure(new RuntimeException(s"Unable to convert $value to expression!"))
  }

  /** Interprets code and returns collection of results for all top-level expressions */
  def interpretVerbosely(code: String): Try[Seq[Try[AnyRef]]] = {
    val results: Try[Seq[models.Expression]] =
      new DebuggerParser(code, models.Context(rootScope)).AllInputLines.run()

    results match {
      case Success(astCollection) =>
        val results = astCollection.map(ast => Try(eval(ast)))
        Success(results.map(_.flatMap(r => Try(r.toScalaValue))))
      case Failure(ex: ParseError)  => Failure(ex)
      case Failure(e)               => Failure(e)
    }
  }

  /** Interprets code and returns result of last top-level expression */
  def interpret(code: String): Try[AnyRef] = interpretVerbosely(code) match {
    case Success(results) => results.last
    case Failure(ex)      => Failure(ex)
  }

  def eval(expression: models.Expression): models.BaseValue =
    eval(expression, rootScope)

  @tailrec
  private def replImpl(): Unit = {
    Console.print("> ")
    Console.flush()
    Console.readLine() match {
      case "" =>
      case line =>
        interpret(line) match {
          case Success(v)               => println(v)
          case Failure(ex: ParseError)  => println(ex.format(line))
          case Failure(ex)              => println(ex)
        }
        replImpl()
    }
  }

  private def eval(
    expression: models.Expression,
    scope: models.Scope
  ): models.BaseValue = expression match {
    case v: models.BaseValue => v
    case models.NoOp() => models.Number(0.0)
    case models.Identifier(name) => eval(getVariable(name, scope), scope)
    case models.ExpressionGroup(exprs) =>
      val newScope = models.Scope.newChildScope(scope)
      exprs.take(exprs.length - 1).foreach(eval(_: models.Expression, newScope))
      eval(exprs.lastOption.getOrElse(models.NoOp()), newScope)
    case models.Conditional(c, t, f) =>
      if (eval(c, scope) == models.Truth(value = true)) eval(t, scope)
      else eval(f, scope)
    case models.Variable(i, e) =>
      scope.variables.put(i, eval(e, scope))
      eval(getVariable(i.name, scope), scope)
    case models.FunctionCall(i, args) => getFunction(i.name, scope) match {
      case models.InterpretedFunction(p, b) =>
        val mappedArgs = args.zipWithIndex.map { case (t, index) =>
          val (identifier, expression) = t
          (if (identifier.name.nonEmpty) identifier else p(index), expression)
        }.toMap
        val evaluatedMappedArgs = mappedArgs.mapValues(eval(_: models.Expression, scope))
        val argScope = models.Scope.newChildScope(scope, evaluatedMappedArgs)
        eval(b, argScope)
      case models.NativeFunction(p, f) =>
        val mappedArgs = args.zipWithIndex.map { case (t, index) =>
          val (identifier, expression) = t
          (if (identifier.name.nonEmpty) identifier else p(index), expression)
        }.toMap
        val evaluatedMappedArgs = mappedArgs.mapValues(eval(_: models.Expression, scope))
        eval(f(evaluatedMappedArgs), scope)
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

  private type I = models.Identifier
  private type E = models.Expression
  private type B = models.BaseValue
  private def toEvalArgs(scope: models.Scope, l: E, r: E): Seq[(I, E)] = Seq(
    models.Identifier("l") -> eval(l, scope),
    models.Identifier("r") -> eval(r, scope)
  )

  private def invokeOperator(name: String, scope: models.Scope, l: E, r: E): B =
    eval(models.FunctionCall(
      models.Identifier(name),
      toEvalArgs(scope, l, r)
    ), scope)

  private def getVariable(
    name: String,
    scope: models.Scope
  ): models.Expression = scope.findVariable(models.Identifier(name)) match {
    case Some(v)  => v
    case None     => throw new RuntimeException(s"Variable $name does not exist!")
  }

  private def getFunction(
    name: String,
    scope: models.Scope
  ): models.Function = scope.findVariable(models.Identifier(name)) match {
    case Some(f: models.Function) => f
    case Some(x) => throw new RuntimeException(s"Function expected, but $x found!")
    case None => throw new RuntimeException(s"Function $name does not exist!")
  }
}
