package org.scaladebugger.api.profiles.frozen.info

import com.sun.jdi.Mirror
import org.scaladebugger.api.profiles.frozen.DataUnavailableFrozenException
import org.scaladebugger.api.profiles.traits.info.CommonInfo
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a frozen implementation of the common info trait.
 */
trait FrozenCommonInfo extends CommonInfo with FrozenJavaInfo {
  /** Unavailable on frozen entity. */
  @throws[UnavailableDataFrozenException]
  override def scalaVirtualMachine: ScalaVirtualMachine =
    throw new UnavailableDataFrozenException("scalaVirtualMachine")

  /** Unavailable on frozen entity. */
  @throws[UnavailableDataFrozenException]
  override def toJdiInstance: Mirror =
    throw new UnavailableDataFrozenException("JDI Instance")
}
