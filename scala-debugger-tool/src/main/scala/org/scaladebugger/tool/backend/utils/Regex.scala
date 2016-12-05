package org.scaladebugger.tool.backend.utils

/**
 * Contains regular expression helper methods.
 */
object Regex {
  /**
   * Generates a regex-compatible wildcard string from normal text.
   *
   * @param text The normal text to convert
   *
   * @return The wildcard string where org.scaladebugger.* is treated as a
   *         wildcard expression in regular expression syntax
   */
  def wildcardString(text: String): String =
    ("\\Q" + text + "\\E").replace("*", "\\E.*\\Q")

  /**
   * Determines if the text contains wildcard characters prior to being
   * transformed into a wildcard string.
   *
   * @param text The normal text to examine
   *
   * @return True if wildcards are detected, otherwise false
   */
  def containsWildcards(text: String): Boolean = text.contains('*')
}
