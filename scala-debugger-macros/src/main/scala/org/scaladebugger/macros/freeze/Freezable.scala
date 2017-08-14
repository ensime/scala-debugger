package org.scaladebugger.macros.freeze

import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.reflect.internal.annotations.compileTimeOnly

/**
 * <summary>Represents a freezable bit of information.</summary>
 *
 * <p>Injects a `freeze()` method along with a `Frozen`
 * class implementing the trait.</p>
 *
 * <p>Any internal method marked with
 * [[org.scaladebugger.macros.freeze.CanFreeze]] will have its live value
 * from an implementation stored at freeze time.</p>
 *
 * <p>Any internal method marked with the
 * [[org.scaladebugger.macros.freeze.CannotFreeze]] annotation will
 * be marked to always throw an exception when accessed.</p>
 *
 * <p>Any internal method not marked with either annotation will keep its
 * current implementation from the trait itself.</p>
 */
@compileTimeOnly("enable macro paradise to expand macro annotations")
class Freezable extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro FreezableMacro.impl
}
