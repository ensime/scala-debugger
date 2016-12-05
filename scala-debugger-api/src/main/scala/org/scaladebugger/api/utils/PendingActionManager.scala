package org.scaladebugger.api.utils

import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a manager of pending actions that can be added and performed.
 *
 * @tparam T The type of information associated with the action
 */
class PendingActionManager[T] {
  import ActionInfo._

  /** Represents the data associated with an action */
  type ActionData = T

  /** Contains a mapping of ids to associated actions */
  private val pendingActions =
    new ConcurrentHashMap[ActionId, Seq[ActionInfo[ActionData]]]().asScala

  /**
   * Adds the action to a new collection of actions.
   *
   * @param actionData The data about the action being added
   * @param action The action to add
   *
   * @return The id of the collection where the action was added
   */
  def addPendingAction(
    actionData: ActionData,
    action: Action
  ): ActionId = {
    addPendingActionWithId(newActionId(), actionData, action)
  }

  /**
   * Adds the action to a collection under the specified id.
   *
   * @param actionId The id of the collection of actions to add to
   * @param actionInfoData The data about the action being added
   * @param action The action to add
   *
   * @return The id of the collection where the action was added
   */
  def addPendingActionWithId(
    actionId: ActionId,
    actionInfoData: ActionData,
    action: Action
  ): ActionId = pendingActions.synchronized {
    val oldActions = pendingActions.getOrElseUpdate(actionId, Nil)
    pendingActions.put(
      actionId,
      oldActions :+ ActionInfo(
        id = actionId,
        data = actionInfoData,
        action = action
      )
    )

    actionId
  }

  /**
   * Processes all actions.
   *
   * @return The collection of action info for successfully-completed actions
   */
  def processAllActions(): Seq[ActionInfo[ActionData]] = {
    pendingActions.synchronized {
      processActionCollectionMap(pendingActions.toMap)
    }
  }

  /**
   * Processes actions whose predicate yields true.
   *
   * @param predicate The predicate to use when looking for actions based on
   *                  their information (true indicates processing action)
   *
   * @return The collection of action info for successfully-completed actions
   */
  def processActions(
    predicate: (ActionInfo[ActionData]) => Boolean
  ): Seq[ActionInfo[ActionData]] = pendingActions.synchronized {
    val actionCollections = pendingActions
      .flatMap(_._2)
      .filter(predicate)
      .groupBy(_.id)
      .mapValues(_.toSeq)
    processActionCollectionMap(actionCollections)
  }

  /**
   * Processes the actions under a collection with the specified id.
   *
   * @param actionId The id of the collection of actions to process
   *
   * @return Some collection of action info for successfully-completed actions
   *         if the collection with the id exists, otherwise None
   */
  def processActionsWithId(
    actionId: ActionId
  ): Option[Seq[ActionInfo[ActionData]]] = pendingActions.synchronized {
    val actionCollection = pendingActions.get(actionId)

    actionCollection
      .map(c => Map(actionId -> c))
      .map(processActionCollectionMap)
  }

  /**
   * Processes a map of collections of actions.
   *
   * @param actionCollectionMap The map of action id -> action collection whose
   *                            actions to process
   *
   * @return The collection of information of actions that were successfully
   *         processed
   */
  protected def processActionCollectionMap(
    actionCollectionMap: Map[ActionId, Seq[ActionInfo[ActionData]]]
  ): Seq[ActionInfo[ActionData]] = pendingActions.synchronized {
    val results = actionCollectionMap.map { case (id, actionCollection) =>
      (id, actionCollection.map(actionInfo => (
        actionInfo,
        Try(actionInfo.action())
      )))
    }

    // Update action list for id with only failed actions
    results.foreach { case (id, actionCollection) =>
      val failedActions = actionCollection.filter(_._2.isFailure).map(_._1)

      pendingActions.put(id, failedActions)
    }

    // Cleanup any invalid action collections
    cleanupActions()

    // Return the information for the successful actions
    results.values.flatMap(_.filter(_._2.isSuccess).map(_._1)).toSeq
  }

  /**
   * Retrieves a collection of actions by the specified id.
   *
   * @param actionId The id of the collection of actions to retrieve
   *
   * @return Some collection of actions if the id exists, otherwise None
   */
  def getPendingActionsWithId(
    actionId: ActionId
  ): Option[Seq[ActionInfo[ActionData]]] = {
    pendingActions.get(actionId)
  }

  /**
   * Retrieves a collection of information for actions with the specified id.
   *
   * @param actionId The id of the collection of actions to retrieve
   *
   * @return Some collection of actions if the id exists, otherwise None
   */
  def getPendingActionDataWithId(
    actionId: ActionId
  ): Option[Seq[ActionData]] = {
    getPendingActionsWithId(actionId).map(_.map(_.data))
  }

  /**
   * Retrieves a collection of actions using the provided predicate.
   *
   * @param predicate The predicate to use when looking for actions based on
   *                  their information (true indicates include the action)
   *
   * @return The collection of actions and their information
   */
  def getPendingActions(
    predicate: (ActionInfo[ActionData]) => Boolean
  ): Seq[ActionInfo[ActionData]] = {
    pendingActions
      .map(_._2.groupBy(predicate))
      .flatMap(_.get(true))
      .flatten
      .toSeq
  }

  /**
   * Retrieves a collection of information for actions using the provided
   * predicate.
   *
   * @param predicate The predicate to use when looking for actions based on
   *                  their information (true indicates include the action)
   *
   * @return The collection of information for actions
   */
  def getPendingActionData(
    predicate: (ActionInfo[ActionData]) => Boolean
  ): Seq[ActionData] = {
    getPendingActions(predicate).map(_.data)
  }

  /**
   * Removes a collection of actions by the specified id.
   *
   * @param actionId The id of the collection of actions to remove
   *
   * @return Some collection of actions if the id exists, otherwise None
   */
  def removePendingActionsWithId(
    actionId: ActionId
  ): Option[Seq[ActionInfo[ActionData]]] = {
    pendingActions.remove(actionId)
  }

  /**
   * Removes any actions using the provided predicate.
   *
   * @param predicate The predicate to use when looking for actions to remove
   *                  based on their information (true indicates removal)
   *
   * @return The collection of removed actions by their info
   */
  def removePendingActions(
    predicate: (ActionInfo[ActionData]) => Boolean
  ): Seq[ActionInfo[ActionData]] = pendingActions.synchronized {
    @volatile var removedActionInfos =
      collection.mutable.Seq[ActionInfo[ActionData]]()

    pendingActions.foreach { case (actionId, actionCollection) =>
      val actionGroup = actionCollection.groupBy(predicate)

      // Add any removed actions to our overall collection
      actionGroup.get(true).foreach(removedActionInfos ++= _)

      // Update the existing collection to include everything else
      pendingActions.put(actionId, actionGroup.getOrElse(false, Nil))
    }

    removedActionInfos.toSeq
  }

  /**
   * Removes any action collection that is null or empty.
   */
  protected def cleanupActions(): Unit = pendingActions.synchronized {
    val actionsToRemove = pendingActions
      .filter(t => t._2 == null || t._2.isEmpty)
      .keys

    actionsToRemove.foreach(pendingActions.remove)
  }

  /**
   * Generates an id for a new action.
   *
   * @return The id as a string
   */
  protected def newActionId(): ActionId = java.util.UUID.randomUUID().toString
}
