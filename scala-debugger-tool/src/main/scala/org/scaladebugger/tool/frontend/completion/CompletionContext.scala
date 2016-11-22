package org.scaladebugger.tool.frontend.completion

import org.scaladebugger.language.interpreters.Interpreter
import org.scaladebugger.language.models.Context

/**
 * Represents a context for completing input.
 *
 * @param methods Method that returns the names of public methods
 * @param variables Method that returns the names of public variables
 */
case class CompletionContext(
  methods: () => Seq[String],
  variables: () => Seq[String]
) {
  /**
   * Returns names of all valid input.
   *
   * @return The combination of names for all valid input
   */
  def allNames: Seq[String] = methods() ++ variables()

  /**
   * Returns names that start with the specified prefix.
   *
   * @param prefix The prefix to check
   *
   * @return All names that start with the specified prefix
   */
  def findWithPrefix(prefix: String): Seq[String] =
    allNames.filter(_.startsWith(prefix))
}

object CompletionContext {
  /**
   * Creates a completion context from a debugger langauge context.
   *
   * @param context The context of the debugger language
   * @return The new completion context instance
   */
  def fromLanguageContext(context: Context): CompletionContext = {
    CompletionContext(
      methods = () => context.functions.map(_._1).map(_.name),
      variables = () => context.variables.map(_._1).map(_.name)
    )
  }
}
