package org.scaladebugger.api.profiles.frozen.info

import java.io.File

import com.sun.jdi.Mirror
import org.scaladebugger.api.profiles.frozen.{DataUnavailableFrozenException, FrozenException}
import org.scaladebugger.api.profiles.traits.info.CommonInfo
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a frozen implementation of the common info trait.
 */
trait FrozenCommonInfo
  extends CommonInfo
    with FrozenJavaInfo
    with FrozenFrozenInfo
{
  /** Unavailable on frozen entity. */
  @throws[FrozenException]
  override def scalaVirtualMachine: ScalaVirtualMachine =
    throw DataUnavailableFrozenException

  /** Unavailable on frozen entity. */
  @throws[FrozenException]
  override def toJdiInstance: Mirror = throw DataUnavailableFrozenException
}
