package org.scaladebugger.language.interpreters
import org.scaladebugger.language.models

import java.io.PrintStream

object DefaultFunctions {
  type IntpFuncArgs = Map[String, Any]
  type IntpFuncRet = Any
  type IntpFunc = (IntpFuncArgs) => IntpFuncRet

  private def GetArg(
    m: IntpFuncArgs,
    name: String,
    default: Option[Any] = None
  ): Any = {
    val value = m.get(name)

    if (value.isEmpty && default.isEmpty) 
      throw new RuntimeException(s"Missing argument $name!")

    value.orElse(default).get
  }

  private val Condition = (op: String, m: IntpFuncArgs) => {
    val l = GetArg(m, "l", default = Some(models.Undefined))
    val r = GetArg(m, "r", default = Some(models.Undefined))

    op match {
      case "<"  => l.toString.toDouble < r.toString.toDouble
      case "<=" => l.toString.toDouble <= r.toString.toDouble
      case ">"  => l.toString.toDouble > r.toString.toDouble
      case ">=" => l.toString.toDouble >= r.toString.toDouble
      case "==" => l == r
      case "!=" => l != r
    }
  }

  val LessThan          = Condition("<", _: IntpFuncArgs)
  val LessThanEqual     = Condition("<=", _: IntpFuncArgs)
  val GreaterThan       = Condition(">", _: IntpFuncArgs)
  val GreaterThanEqual  = Condition(">=", _: IntpFuncArgs)
  val Equal             = Condition("==", _: IntpFuncArgs)
  val NotEqual          = Condition("!=", _: IntpFuncArgs)

  private val NumberOperation = (op: String, m: IntpFuncArgs) => {
    val l = GetArg(m, "l").toString.toDouble
    val r = GetArg(m, "r").toString.toDouble

    op match {
      case "+" => l + r
      case "-" => l - r
      case "*" => l * r
      case "/" => l / r
      case "%" => l % r
    }
  }

  private val StringOperation = (op: String, m: IntpFuncArgs) => {
    val l = GetArg(m, "l").toString
    val r = GetArg(m, "r").toString

    op match {
      case "++" => l ++ r
    }
  }

  val PlusPlus  = StringOperation("++", _: IntpFuncArgs)
  val Plus      = NumberOperation("+", _: IntpFuncArgs)
  val Minus     = NumberOperation("-", _: IntpFuncArgs)
  val Multiply  = NumberOperation("*", _: IntpFuncArgs)
  val Divide    = NumberOperation("/", _: IntpFuncArgs)
  val Modulus   = NumberOperation("%", _: IntpFuncArgs)

  private val PrintOperation = (out: PrintStream, m: IntpFuncArgs) => {
    val text = GetArg(m, "text").toString

    out.println(text)
  }

  val Print = (p: PrintStream) => PrintOperation(p, _: IntpFuncArgs)
}
