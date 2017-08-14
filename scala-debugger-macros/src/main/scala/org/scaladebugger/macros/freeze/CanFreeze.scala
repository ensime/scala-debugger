package org.scaladebugger.macros.freeze

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

/**
 * Marks an internal method of a [[org.scaladebugger.macros.freeze.Freezable]]
 * component as able to be frozen, which means it will be swapped with the
 * result of accessing the live data when frozen.
 */
class CanFreeze extends StaticAnnotation
