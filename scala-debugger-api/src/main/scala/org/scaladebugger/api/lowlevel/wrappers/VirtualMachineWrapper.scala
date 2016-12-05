package org.scaladebugger.api.lowlevel.wrappers

import com.sun.jdi.{Value, VirtualMachine}

/**
 * Represents a wrapper around a value, providing additional methods.
 *
 * @param _virtualMachine The virtual machine to wrap
 */
class VirtualMachineWrapper(private val _virtualMachine: VirtualMachine) {
  require(_virtualMachine != null, "Virtual machine cannot be null!")

  /**
   * Creates a mirror for the specified value if possible, otherwise
   * throws an exception.
   *
   * @param value The value to mirror on the remote JVM
   *
   * @return The mirrored instance
   */
  @throws[RuntimeException]
  def mirrorOf(value: Any): Value = value match {
    case v: Boolean => _virtualMachine.mirrorOf(v)
    case v: Byte    => _virtualMachine.mirrorOf(v)
    case v: Char    => _virtualMachine.mirrorOf(v)
    case v: Int     => _virtualMachine.mirrorOf(v)
    case v: Short   => _virtualMachine.mirrorOf(v)
    case v: Long    => _virtualMachine.mirrorOf(v)
    case v: Double  => _virtualMachine.mirrorOf(v)
    case v: Float   => _virtualMachine.mirrorOf(v)
    case v: String  => _virtualMachine.mirrorOf(v)
    case x          =>
      val name = x.getClass.getName
      throw new RuntimeException(s"Unsupported value type: $name")
  }
}
