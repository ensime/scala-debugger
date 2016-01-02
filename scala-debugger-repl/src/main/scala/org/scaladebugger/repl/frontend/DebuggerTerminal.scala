package org.scaladebugger.repl.frontend

import java.io.OutputStreamWriter

import ammonite.terminal._
import ammonite.terminal.LazyList.~:
import org.parboiled2.ParseError
import org.scaladebugger.repl.language.interpreters.DebuggerInterpreter

import scala.annotation.tailrec
import scala.util.{Failure, Success}

class DebuggerTerminal {
  private val interpreter = new DebuggerInterpreter

  def run(): Unit = {
    var history = List.empty[String]
    val selection = GUILikeFilters.SelectionFilter(indent = 4)
    def multilineFilter: TermCore.Filter = {
      case TermState(13 ~: rest, b, c) if b.count(_ == '(') != b.count(_ == ')') =>
        BasicFilters.injectNewLine(b, c, rest)
    }
    val reader = new java.io.InputStreamReader(System.in)
    rec()
    @tailrec def rec(): Unit = {
      TermCore.readLine(
        Console.MAGENTA + "@ " + Console.RESET,
        reader,
        new OutputStreamWriter(System.out),
        multilineFilter orElse
          selection orElse
          BasicFilters.tabFilter(4) orElse
          GUILikeFilters.altFilter orElse
          GUILikeFilters.fnFilter orElse
          ReadlineFilters.navFilter orElse
          ReadlineFilters.CutPasteFilter() orElse
          ReadlineFilters.HistoryFilter(() => history) orElse
          BasicFilters.all,
        // Example displayTransform: underline all non-spaces
        displayTransform = (buffer, cursor) => {
          // underline all non-blank lines
          def hl(b: Vector[Char]): Vector[Char] = b.flatMap{
            case ' ' => " "
            case '\n' => "\n"
            case c => Console.UNDERLINED + c + Console.RESET
          }
          // and highlight the selection
          selection.mark match{
            case Some(mark) if mark != cursor =>
              val Seq(min, max) = Seq(mark, cursor).sorted
              val (a, b0) = buffer.splitAt(min)
              val (b, c) = b0.splitAt(max - min)
              val displayOffset = if (cursor < mark) 0 else -1
              (
                hl(a) ++ Console.REVERSED ++ b ++ Console.RESET ++ hl(c),
                displayOffset
                )
            case _ => (hl(buffer), 0)
          }

        }
      ) match {
        case None => println("Bye!")
        case Some(s) =>
          history = s :: history
          //println(s)
          interpreter.interpret(s) match {
            case Success(v)               => println(v)
            case Failure(ex: ParseError)  => println(ex.format(s))
            case Failure(ex)              => println(ex)
          }
          println(interpreter.interpret(s))
          rec()
      }
    }
  }

}
