package org.scaladebugger.macros.freeze

import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
import org.scaladebugger.macros.freeze.CanFreeze.ReturnType.ReturnType

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

/**
 * Marks an internal method of a [[org.scaladebugger.macros.freeze.Freezable]]
 * component as able to be frozen, which means it will be swapped with the
 * result of accessing the live data when frozen.
 *
 * @param returnType Freeze-related information about the method's return type
 *                   (defaults to normal freezing with no additional work)
 */
case class CanFreeze(
  returnType: ReturnType = ReturnType.Normal
) extends StaticAnnotation

object CanFreeze {
  /** Return type of method being frozen. */
  object ReturnType extends Enumeration {
    type ReturnType = Value
    val Normal, FreezeObject, FreezeCollection,
    FreezeOption, FreezeEither = Value
  }
}
