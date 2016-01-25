package org.scaladebugger.repl.frontend

import ammonite.terminal.SpecialKeys.Ctrl

/**
 * Represents a fallback terminal that does not have color or multi-line
 * support.
 */
class FallbackTerminal extends Terminal {
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
}
