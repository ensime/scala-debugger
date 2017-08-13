package org.scaladebugger.macros.freeze
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

/**
 * Marks an internal method of a [[org.scaladebugger.macros.freeze.Freezable]]
 * component as unable to be frozen, which means it will be swapped with an
 * exception when frozen.
 */
class Unfreezable extends StaticAnnotation
