package org.scaladebugger.repl.frontend

import org.scaladebugger.language.models.{Context, Identifier}

/**
 * Contains helper methods to do with terminal operations.
 */
object TerminalUtilities {
  /**
   * Determines if a given line represents a help request.
   *
   * @param line The line of text to parse
   *
   * @return True if the line represents a help request, otherwise false
   */
  def isHelpRequest(line: String): Boolean = {
    val tokens = line.trim.split(" ")

    tokens.nonEmpty && tokens.head == "help"
  }

  /**
   * Given the context and line, prints help information either for a specific
   * topic or a list of all topics.
   *
   * @param context The context with which to retrieve topics
   * @param line The line indicating what topic(s) to print
   * @param maxWidth The maximum width of the terminal, used for column-based
   *                 alignment
   */
  def printHelp(context: Context, line: String, maxWidth: Int = 80): Unit = {
    val tokens = line.trim.split(" ")
    val arguments = tokens.tail
    val topicMap = context.functions
      .map(t => t._1 -> t._2.documentation)
      .filter(_._2.nonEmpty)
      .map(t => t._1 -> t._2.get)
      .toMap

    // TODO: Enable help outside of functions (so we can display inline)
    if (arguments.nonEmpty) {
      arguments.foreach(topic => {
        val documentation = topicMap.getOrElse(
          Identifier(topic),
          "No documentation found!"
        )

        val formatString = "%s(%s):\n\n%s\n\n"
        val indentation = 4
        val fArgString = context.functions.toMap.find(_._1.name == topic)
            .map(_._2.parameters.map(_.name).mkString(",")).getOrElse("")
        val formattedDoc = documentation.grouped(maxWidth - indentation)
          .map(" " * indentation + _).mkString("\n")
        Console.out.printf(formatString, topic, fArgString, formattedDoc)
      })
    } else {
      val maxColumns = maxWidth / 20
      val topics = topicMap.keySet.map(_.name).toSeq.sorted

      val title = "Available Topics"
      Console.out.println(title)
      Console.out.println("=" * title.length)

      val printCollection = topics.map(_ => "%-19s ").zip(topics)
      printCollection.grouped(maxColumns).foreach(a => {
        val formatString = a.map(_._1).mkString("") + "\n"
        val data = a.map(_._2)
        Console.out.printf(formatString, data: _*)
      })
    }
  }
}
