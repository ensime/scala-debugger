package org.scaladebugger.language.parsers

import org.parboiled2._
import org.scaladebugger.language.models.Context
import org.scaladebugger.language.parsers.grammar.FullGrammar

/**
 * Represents the standard implementation of a parser for the
 * small debugger language.
 *
 * @param input The input to be parsed
 */
class DebuggerParser(
  val input: ParserInput,
  val context: Context = Context.blank
) extends Parser with FullGrammar
