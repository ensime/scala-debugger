package org.scaladebugger.language.models

import scala.annotation.tailrec

/**
 * Represents a scope within the language containing declared variables as
 * well as the parent scope if it exists.
 *
 * @param variables The variables associated with the scope
 * @param parent Some scope if a parent exists, otherwise None
 */
case class Scope(
  variables: collection.mutable.Map[Identifier, Expression],
  parent: Option[Scope]
) {
  /**
   * Indicates whether or not this scope is a root scope.
   *
   * @return True if this scope has no parent, otherwise false
   */
  def isRoot: Boolean = parent.isEmpty

  /**
   * Retrieves the variable with the associated identifier, recursing through
   * parent scopes until the variable is found.
   *
   * @param identifier The identifier for the variable to look up
   * @return Some variable value if found, otherwise None
   */
  def findVariable(identifier: Identifier): Option[Expression] =
    findVariable(identifier, this, recursion = true)

  @tailrec private def findVariable(
    identifier: Identifier,
    scope: Scope,
    recursion: Boolean
  ): Option[Expression] = {
    if (scope.variables.contains(identifier)) scope.variables.get(identifier)
    else if (!recursion || scope.parent.isEmpty) None
    else findVariable(identifier, scope.parent.get, recursion)
  }
}

object Scope {
  /**
   * Creates a new root scope.
   *
   * @param initialVariables Any variables to provide to the scope
   * @return The new scope instance
   */
  def newRootScope(
    initialVariables: Map[Identifier, Expression] = Map()
  ): Scope = newChildScope(null, initialVariables)

  /**
   * Creates a new scope from the provided parent and initial variables.
   *
   * @param parent The parent scope (null indicates no parent)
   * @param initialVariables Any variables to provide to the scope
   * @return The new scope instance
   */
  def newChildScope(
    parent: Scope,
    initialVariables: Map[Identifier, Expression] = Map()
  ): Scope = Scope(
    collection.mutable.Map(initialVariables.toSeq: _*),
    Option(parent)
  )
}
