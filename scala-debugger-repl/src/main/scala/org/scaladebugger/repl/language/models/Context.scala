package org.scaladebugger.repl.language.models

/**
 * Represents the context about the program state at a point in time.
 *
 * @param currentScope The local scope of the program at a point in time
 */
case class Context(
  currentScope: Scope
) {
  /**
   * Retrieves all available functions relative to this context.
   *
   * @return The collection of functions and their associated names
   */
  def functions: Seq[(Identifier, Function)] = currentScope.variables.flatMap {
    case (i: Identifier, f: InterpretedFunction)  => Some((i, f))
    case (i: Identifier, f: NativeFunction)       => Some((i, f))
    case _                                        => None
  }.toSeq ++ currentScope.parent.map(Context.apply).map(_.functions).toSeq.flatten

  /**
   * Retrieves the available interpreted functions relative to this context.
   *
   * @return The collection of interpreted functions and their associated names
   */
  def interpretedFunctions: Seq[(Identifier, InterpretedFunction)] =
    functions.collect { case (i: Identifier, f: InterpretedFunction) => (i, f) }

  /**
   * Retrieves the available native functions relative to this context.
   *
   * @return The collection of native functions and their associated names
   */
  def nativeFunctions: Seq[(Identifier, NativeFunction)] =
    functions.collect { case (i: Identifier, f: NativeFunction) => (i, f) }
}

object Context {
  /**
   * Represents a context with an empty scope.
   *
   * @return The context instance
   */
  lazy val blank: Context = Context(Scope.newRootScope())
}
