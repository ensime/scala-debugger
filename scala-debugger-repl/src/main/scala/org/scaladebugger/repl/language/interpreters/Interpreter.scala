package org.scaladebugger.repl.language.interpreters

import scala.util.Try

/**
 * Created by senkwich on 1/1/16.
 */
trait Interpreter {
  def interpret(code: String): Try[AnyRef]

  def put(name: String, value: Any): Unit

  def get(name: String): Option[Any]
}
