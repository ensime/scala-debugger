package org.scaladebugger.repl.language.interpreters

import java.io.{BufferedInputStream, PrintStream}

object DefaultFunctions {
  type IntpFuncArgs = Map[String, Any]
  type IntpFuncRet = Any
  type IntpFunc = (IntpFuncArgs) => IntpFuncRet

  private val GetArg = (m: IntpFuncArgs, name: String) => {
    val value = m.get(name)
    if (value.isEmpty) throw new RuntimeException(s"Missing argument $name!")
    value.get
  }

  private val Condition = (op: String, m: IntpFuncArgs) => {
    val l = GetArg(m, "l")
    val r = GetArg(m, "r")

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
