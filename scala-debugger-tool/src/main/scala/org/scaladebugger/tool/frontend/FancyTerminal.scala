package org.scaladebugger.tool.frontend
import acyclic.file

import java.io.OutputStreamWriter

import ammonite.terminal.{Terminal => TermCore, _}
import ammonite.terminal.filters._
import ammonite.terminal.LazyList.~:
import org.scaladebugger.language.parsers.grammar.ReservedKeywords

/**
 * Represents a fancy terminal that provides color and multi-line support.
 */
class FancyTerminal extends Terminal {
  @volatile private var history = List.empty[String]

  val selection = GUILikeFilters.SelectionFilter(indent = 4)
  val reader = new java.io.InputStreamReader(System.in)

  override def readLine(): Option[String] = {
    val line = TermCore.readLine(
      Console.MAGENTA + prompt() + Console.RESET,
      reader,
      new OutputStreamWriter(System.out),
      Filter.merge(
        multilineFilter,
        selection,
        BasicFilters.tabFilter(4),
        GUILikeFilters.altFilter,
        GUILikeFilters.fnFilter,
        ReadlineFilters.navFilter,
        ReadlineFilters.CutPasteFilter(),
        new HistoryFilter(() => history.toVector, fansi.Color.Blue),
        BasicFilters.all
      ),
      displayTransform = mainDisplayTransform(selection, _: Vector[Char], _: Int)
    )

    line.map(_.trim).filter(_.nonEmpty).foreach(l => history = l :: history)

    line
  }

  private def mainDisplayTransform(
    selection: GUILikeFilters.SelectionFilter,
    buffer: Vector[Char],
    cursor: Int
  ): (fansi.Str, Int) = {
    val data: (Vector[Char], Int) = selection.mark match {
      case Some(mark) if mark != cursor =>
        val Seq(min, max) = Seq(mark, cursor).sorted
        val (a, b0) = buffer.splitAt(min)
        val (b, c) = b0.splitAt(max - min)
        val displayOffset = if (cursor < mark) 0 else -1
        (hl(a) ++ Console.REVERSED ++ b ++ Console.RESET ++ hl(c), displayOffset)
      case _ => (hl(buffer), 0)
    }

    (fansi.Str(data._1), data._2)
  }

  /**
   * Represents a filter to allow multi-line statements using various block
   * structures.
   *
   * @return The filter for multi-line support
   */
  private def multilineFilter: Filter = Filter.partial {
    case TermState(13 ~: rest, b, c, _) if b.count(_ == '(') != b.count(_ == ')') =>
      BasicFilters.injectNewLine(b, c, rest)
    case TermState(13 ~: rest, b, c, _) if b.count(_ == '{') != b.count(_ == '}') =>
      BasicFilters.injectNewLine(b, c, rest)
  }

  /**
   * Highlights keywords in the provided collection of characters.
   *
   * @param b The collection of characters whose keywords to highlight
   * @return The resulting collection of characters containing highlight markers
   */
  private def hl(b: Vector[Char]): Vector[Char] = {
    import scala.collection.JavaConverters._
    @volatile var keywords = collection.mutable.Seq[java.util.LinkedList[Char]]()
    @volatile var currentKeyword = new java.util.LinkedList[Char]()
    def newKeyword(s: String) = new java.util.LinkedList[Char](s.toList.asJava)
    def nextKeyword() = {
      currentKeyword = new java.util.LinkedList[Char]()
      keywords :+= currentKeyword
    }

    nextKeyword()

    // NOTE: Treating " as single keyword to make highlighting easier
    b.foreach {
      case c if c.isLetterOrDigit => currentKeyword.add(c)
      case c if c == '"' => keywords :+= newKeyword(c.toString); nextKeyword()
      case c => keywords :+= newKeyword(c.toString); nextKeyword()
    }

    lazy val checkQuotes: String => Boolean = (() => {
      @volatile var withinQuotes = false

      (keyword: String) => {
        if (keyword != null && keyword.trim == "\"") {
          withinQuotes = !withinQuotes
          true
        } else withinQuotes
      }
    })()
    val updatedKeywords = keywords.map(k=> new String(k.asScala.toArray)).flatMap {
      case k if checkQuotes(k) =>
        k.flatMap(Console.GREEN + _ + Console.RESET)
      case k if ReservedKeywords.Values.contains(k) =>
        k.flatMap(Console.CYAN + _ + Console.RESET)
      case k if ReservedKeywords.NonValues.contains(k) =>
        k.flatMap(Console.RED + _ + Console.RESET)
      case k =>
        k
    }.toVector
    updatedKeywords
  }
}
