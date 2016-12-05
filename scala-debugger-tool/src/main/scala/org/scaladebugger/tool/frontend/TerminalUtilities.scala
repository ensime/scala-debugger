package org.scaladebugger.tool.frontend
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
      .map(t => t._1.name -> t._2.get)
      .toMap

    // TODO: Enable help outside of functions (so we can display inline)
    if (arguments.nonEmpty) {
      arguments.foreach(topic => {
        val documentation =
          topicMap.getOrElse(topic, "No function documentation found!")

        val formatString = "%s(%s):\n\n%s\n\n"

        @inline def indent(text: String, indentation: Int = 4) =
          " " * indentation + text
        @inline def wrap(text: String, indentation: Int = 4) = {
          val words = text.split(" ")
          val lines = words.foldLeft(Seq[String]()) { case (acc, w) =>
            // If no words added yet, add our first one as a line
            if (acc.isEmpty)
              Seq(w)
            // If words exist, but the new word doesn't overstep our length,
            // add it to the last line
            else if (acc.last.length + w.length + 1 <= maxWidth - indentation)
              acc.take(acc.size - 1) :+ acc.last + " " + w
            // If the new word oversteps last line, add new line with word
            else
              acc :+ w
          }

          lines.map(l => indent(l, indentation)).mkString("\n")
        }

        val func = context.functions.toMap.find(_._1.name == topic)
        val funcArgString = func.map(
          _._2.parameters.map(_.name).mkString(",")
        ).getOrElse("")
        val formattedArgDocs = func.map { case(i, f) =>
          val s = f.parameters.filter(_.documentation.exists(_.nonEmpty))
            .map(p => {
              val prefix = indent(p.name + ": ")
              prefix + wrap(
                p.documentation.get,
                indentation = prefix.length
              ).dropWhile(_.isWhitespace)
            })
            .mkString("\n")
          if (s.nonEmpty) s + "\n\n"
          else ""
        }.getOrElse("")
        val formattedFuncDoc = wrap(documentation)
        val formattedDoc = formattedArgDocs + formattedFuncDoc
        Console.out.printf(formatString, topic, funcArgString, formattedDoc)
      })
    } else {
      val maxColumns = maxWidth / 20
      val topics = topicMap.keySet.toSeq.sorted

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
