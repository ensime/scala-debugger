package org.scaladebugger.api.utils

import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConverters._

/**
 * Represents a data structure of mappings for multiple values.
 *
 * @tparam Key The key used to go from Key -> Seq[Id]
 * @tparam Value The value yielded from Key or any Id
 */
class MultiMap[Key, Value] {
  /** Represents an id to map key to value */
  type Id = String

  /** Contains mapping of key -> collection of ids */
  private val keyToIds = new ConcurrentHashMap[Key, Seq[Id]]().asScala

  /** Contains mapping of id -> value */
  private val idToValue = new ConcurrentHashMap[Id, Value]().asScala

  /**
   * Retrieves all keys stored in this map.
   *
   * @return The collection of keys
   */
  def keys: Seq[Key] = keyToIds.keySet.toSeq

  /**
   * Retrieves all underlying ids stored in this map used to link
   * keys to values.
   *
   * @return The collection of ids
   */
  def ids: Seq[Id] = idToValue.keySet.toSeq

  /**
   * Retrieves all values stored in this map.
   *
   * @return The collection of values
   */
  def values: Seq[Value] = idToValue.values.toSeq

  /**
   * Adds the value with the specified key to the map. Uses the specified id
   * to link the key with the value.
   *
   * @param id The id to use to link the key with the value
   * @param key The key to use when looking up and removing the value
   * @param value The value to store in the map
   *
   * @return The underlying id used to link the key with the value
   */
  def putWithId(id: Id, key: Key, value: Value): Id = {
    require(!idToValue.keySet.contains(id), "Id must be unique!")

    val oldIds = keyToIds.getOrElseUpdate(key, Nil)
    keyToIds.put(key, oldIds :+ id)

    idToValue.put(id, value)

    id
  }

  /**
   * Adds the value with the specified key to the map.
   *
   * @param key The key to use when looking up and removing the value
   * @param value The value to store in the map
   *
   * @return The underlying id used to link the key with the value
   */
  def put(key: Key, value: Value): Id = {
    putWithId(newId(), key, value)
  }

  /**
   * Removes the values with the specified key. Also, removes the underlying
   * ids linking the key to the collection of values.
   *
   * @param key The key of the values to remove
   *
   * @return Some collection of values if the key exists, otherwise None
   */
  def remove(key: Key): Option[Seq[Value]] = {
    keyToIds.remove(key).map(_.flatMap(idToValue.remove(_: Id)))
  }

  /**
   * Removes all values whose key satisfies the specified predicate.
   *
   * @param predicate The predicate to use when evaluating keys
   *
   * @return The collection of removed values
   */
  def removeWithKeyPredicate(predicate: Key => Boolean): Seq[Value] = {
    this.keys.filter(predicate).flatMap(this.remove).flatten
  }

  /**
   * Removes all values that satisfy the specified predicate.
   *
   * @param predicate The predicate to use when evaluating values
   *
   * @return The collection of removed values
   */
  def removeWithValuePredicate(predicate: Value => Boolean): Seq[Value] = {
    idToValue.filter(t => predicate(t._2)).keys.flatMap(this.removeWithId).toSeq
  }

  /**
   * Removes the value with the specified id. Also, removes the association
   * between a key and the value via the id.
   *
   * @param id The id of the value to remove
   *
   * @return Some value if the id exists, otherwise None
   */
  def removeWithId(id: Id): Option[Value] = {
    val removedValue = idToValue.remove(id)

    // Reverse-lookup and remove id from key
    keyToIds.mapValues(_.filterNot(_ == id)).foreach { case (key, value) =>
      if (value.nonEmpty) keyToIds.put(key, value)
      else keyToIds.remove(key)
    }

    removedValue
  }

  /**
   * Determines if the specified key exists in the map.
   *
   * @param key The key to check
   *
   * @return True if the key exists, otherwise false
   */
  def has(key: Key): Boolean = {
    get(key).nonEmpty
  }

  /**
   * Determines if any key satisfies the specified predicate.
   *
   * @param predicate The predicate to use when evaluating keys
   *
   * @return True if a key satisfies the predicate, otherwise false
   */
  def hasWithKeyPredicate(predicate: Key => Boolean): Boolean = {
    this.keys.exists(predicate)
  }

  /**
   * Determines if any value satisfies the specified predicate.
   *
   * @param predicate The predicate to use when evaluating values
   *
   * @return True if a value satisfies the predicate, otherwise false
   */
  def hasWithValuePredicate(predicate: Value => Boolean): Boolean = {
    this.values.exists(predicate)
  }

  /**
   * Determines if the specified id exists in the map.
   *
   * @param id The id to check
   *
   * @return True if the id exists, otherwise false
   */
  def hasWithId(id: Id): Boolean = {
    getWithId(id).nonEmpty
  }

  /**
   * Retrieves all values for the specified key.
   *
   * @param key The key whose values to retrieve
   *
   * @return Some collection of values if the key exists, otherwise None
   */
  def get(key: Key): Option[Seq[Value]] = {
    keyToIds.get(key).map(_.flatMap(getWithId))
  }

  /**
   * Retrieves all values whose key satisfies the specified predicate.
   *
   * @param predicate The predicate to use when evaluating keys
   *
   * @return The collection of values whose keys satisfied the predicate
   */
  def getWithKeyPredicate(predicate: Key => Boolean): Seq[Value] = {
    this.keys.filter(predicate).flatMap(this.get).flatten
  }

  /**
   * Retrieves all values that satisfy the specified predicate.
   *
   * @param predicate The predicate to use when evaluating values
   *
   * @return The collection of values that satisfied the predicate
   */
  def getWithValuePredicate(predicate: Value => Boolean): Seq[Value] = {
    this.values.filter(predicate)
  }

  /**
   * Retrieves the value for the specified id.
   *
   * @param id The id of the value to retrieve
   *
   * @return Some value if the id exists, otherwise None
   */
  def getWithId(id: Id): Option[Value] = {
    idToValue.get(id)
  }

  /**
   * Retrieves the collection of ids for an key.
   *
   * @param key The key whose ids to retrieve
   *
   * @return Some collection of ids if the key exists, otherwise None
   */
  def getIdsWithKey(key: Key): Option[Seq[Id]] = {
    keyToIds.get(key)
  }

  /**
   * Retrieves the collection of ids for all keys that satisfy the predicate.
   *
   * @param predicate The predicate to use when evaluating keys
   *
   * @return The collection of ids whose keys satisfy the predicate
   */
  def getIdsWithKeyPredicate(predicate: Key => Boolean): Seq[Id] = {
    keyToIds.filter(t => predicate(t._1)).values.flatten.toSeq
  }

  /**
   * Retrieves the collection of ids for all values that satisfy the predicate.
   *
   * @param predicate The predicate to use when evaluating values
   *
   * @return The collection of ids whose values satisfy the predicate
   */
  def getIdsWithValuePredicate(predicate: Value => Boolean): Seq[Id] = {
    idToValue.filter(t => predicate(t._2)).keys.toSeq
  }

  /**
   * Retrieves the key that uses the specified id to map to a value.
   *
   * @param id The id used to associate a value with the desired key
   *
   * @return Some key if the id exists, otherwise None
   */
  def getKeyWithId(id: Id): Option[Key] = {
    keyToIds.find(_._2.contains(id)).map(_._1)
  }

  /**
   * Generates a new id.
   *
   * @return The new id from a UUID
   */
  protected def newId(): Id = java.util.UUID.randomUUID().toString
}
