package org.scaladebugger.macros.freeze

/**
 * Exception indicating that a particular construct was not frozen during
 * a freeze request.
 */
class NotFrozenException extends Exception with Serializable
