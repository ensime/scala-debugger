package org.scaladebugger.macros.freeze

import org.scaladebugger.macros.freeze.FreezeMetadata.ReturnType.ReturnType

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

/**
 * Internal metadata assigned to methods upon inspection.
 *
 * @param returnType Freeze-related information about the method's return type
 */
case class FreezeMetadata private[freeze](
  returnType: ReturnType
) extends StaticAnnotation

object FreezeMetadata {
  /** Return type of method being frozen. */
  object ReturnType extends Enumeration {
    type ReturnType = Value
    val Normal, FreezeObject, FreezeCollection,
    FreezeOption, FreezeEither = Value
  }
}
