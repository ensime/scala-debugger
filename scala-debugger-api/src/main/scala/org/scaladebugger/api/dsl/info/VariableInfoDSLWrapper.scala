package org.scaladebugger.api.dsl.info

import org.scaladebugger.api.profiles.traits.info.{ObjectInfo, VariableInfo}
import org.scaladebugger.api.virtualmachines.ObjectCache

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param variableInfo The profile to wrap
 */
class VariableInfoDSLWrapper private[dsl] (
  private val variableInfo: VariableInfo
) {
  /**
   * Caches the value of this variable in its associated JVM cache.
   *
   * @param objectCache The JVM cache to store this variable's value
   * @return The variable whose value was stored
   */
  def cache()(
    implicit objectCache: ObjectCache = variableInfo.scalaVirtualMachine.cache
  ): VariableInfo = {
    import org.scaladebugger.api.dsl.Implicits.ValueInfoDSL
    variableInfo.toValueInfo.cache()

    variableInfo
  }

  /**
   * Removes this variable's value from its associated JVM cache.
   *
   * @param objectCache The JVM cache to remove this variable's value
   * @return The variable whose value was removed
   */
  def uncache()(
    implicit objectCache: ObjectCache = variableInfo.scalaVirtualMachine.cache
  ): VariableInfo = {
    import org.scaladebugger.api.dsl.Implicits.ValueInfoDSL
    variableInfo.toValueInfo.uncache()

    variableInfo
  }
}
