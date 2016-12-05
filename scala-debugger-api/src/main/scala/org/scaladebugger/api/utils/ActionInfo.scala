package org.scaladebugger.api.utils

import org.scaladebugger.api.utils.ActionInfo._

/**
 * Represents the information about an action.
 *
 * @tparam T The type of additional data associated with the action
 * @param id The id associated with the action
 * @param data The additional data associated with the action
 * @param action The action as a function
 */
case class ActionInfo[T](id: ActionId, data: T, action: Action) {
  type ActionData = T
}

/**
 * Contains constants and type information.
 */
object ActionInfo {
  /** Represents an id for one or more actions */
  type ActionId = String

  /** Represents an action that can be performed */
  type Action = () => Unit
}
