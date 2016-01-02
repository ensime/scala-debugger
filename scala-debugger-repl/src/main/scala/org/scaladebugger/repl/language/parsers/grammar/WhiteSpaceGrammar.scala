package org.scaladebugger.repl.language.parsers.grammar

import org.parboiled2._
import org.scaladebugger.repl.language.models

/**
 * Contains whitespace-related grammars.
 */
trait WhiteSpaceGrammar extends Parser {
  val NewLineChar = CharPredicate("\n\r")
  val NoNewLineWhiteSpaceChar = CharPredicate(" \t\f")
  val WhiteSpaceChar = NoNewLineWhiteSpaceChar ++ NewLineChar

  def NoNewLineWhiteSpace: Rule0 = rule { zeroOrMore(NoNewLineWhiteSpaceChar) }
  def NewLine: Rule0 = rule { zeroOrMore(NewLineChar) }
  def WhiteSpace: Rule0 = rule { zeroOrMore(WhiteSpaceChar) }

  def AtLeastOneNoNewLineWhiteSpace: Rule0 = rule {
    oneOrMore(NoNewLineWhiteSpaceChar)
  }
  def AtLeastOneNewLine: Rule0 = rule { oneOrMore(NewLineChar) }
  def AtLeastOneWhiteSpace: Rule0 = rule { oneOrMore(WhiteSpaceChar) }

  def ws(c: Char): Rule0 = rule { WhiteSpace ~ c ~ WhiteSpace }
  def ws(s: String): Rule0 = rule { WhiteSpace ~ s ~ WhiteSpace }
}
