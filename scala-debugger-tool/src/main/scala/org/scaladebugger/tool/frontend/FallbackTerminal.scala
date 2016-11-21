package org.scaladebugger.tool.frontend
import acyclic.file
import ammonite.terminal.SpecialKeys.Ctrl
import org.scaladebugger.tool.frontend.history.{HistoryManager, NoopHistoryManager}

/**
 * Represents a fallback terminal that does not have color or multi-line
 * support.
 */
class FallbackTerminal extends Terminal {
  /**
   * Returns the manager used to keep track of this terminal's history.
   *
   * @return
   */
  override def history: HistoryManager = NoopHistoryManager.Instance

  /**
   * Reads the next line from the terminal.
   *
   * @return Some line if found, otherwise None if EOF reached
   */
  override def readLine(): Option[String] = {
    Console.print(prompt())
    Console.flush()

    @volatile var stop = false
    val lineBuilder = new StringBuilder
    val inputStream = Console.in
    Stream.continually(inputStream.read()).takeWhile(i => {
      // Treat -1 as Ctrl+D
      if (i < 0) {
        stop = true
        false
      } else {
        val c = i.toChar
        if (c.toString == Ctrl('d')) false
        else if (c == '\n') false
        else true
      }
    }).map(_.toChar).foreach(lineBuilder.append)

    if (!stop) Some(lineBuilder.toString().trim) else None
  }

  /**
   * Writes the provided text to the terminal.
   *
   * @param text The text to write out to the terminal
   */
  override def write(text: String): Unit = Console.print(text)
}
