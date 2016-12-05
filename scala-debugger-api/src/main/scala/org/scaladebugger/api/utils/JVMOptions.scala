package org.scaladebugger.api.utils

/**
 * Represents a collection of JVM options.
 */
case class JVMOptions(
  properties: Map[String, String],
  options: Map[String, String]
)

object JVMOptions {
  /**
   * Parses a line of JVM properties and assigns them to a JVMOptions object.
   *
   * @param line The line of JVM properties to parse
   *
   * @return The new JVMOptions instance containing all parsed options
   */
  def fromOptionString(line: String): JVMOptions = {
    require(line != null, "Line cannot be null!")
    val tokens = line.split(" ")

    @volatile var inQuotes = false
    @volatile var partialToken = ""
    val allInput = tokens.foldLeft(Map[String, String]()) { case (current, token) =>
      var skipToNextToken = false
      var fullToken = token

      if (totalQuotes(token) % 2 == 1) {
        if (inQuotes) {
          fullToken = partialToken + token
          partialToken = ""
        } else {
          partialToken += token + " "
          skipToNextToken = true
        }
        inQuotes = !inQuotes
      } else if (inQuotes) {
        partialToken += token + " "
        skipToNextToken = true
      }

      if (skipToNextToken || fullToken.trim.isEmpty) {
        current
      } else if (isKeyValuePair(fullToken)) {
        val splitToken = fullToken.trim.split("=")
        val (key, value) = (splitToken.head, splitToken.tail.mkString("="))
        val cleanedValue = value.stripPrefix("\"").stripSuffix("\"")
        current ++ Map(key -> cleanedValue)
      } else {
        current ++ Map(fullToken -> "")
      }
    }

    val properties = allInput.filterKeys(_.startsWith("-D")).map { case (k, v) =>
      k.stripPrefix("-D") -> v
    }

    val options = allInput.filterKeys(k => !k.startsWith("-D")).map { case (k, v) =>
      k.stripPrefix("-").stripPrefix("-") -> v
    }

    JVMOptions(
      properties = properties,
      options = options
    )
  }

  private def isKeyValuePair(token: String): Boolean = token.contains("=")
  private def totalQuotes(token: String): Int = token.count(_ == '"')

  /** Represents an empty collection of JVM options. */
  lazy val Blank = JVMOptions(Map(), Map())
}
