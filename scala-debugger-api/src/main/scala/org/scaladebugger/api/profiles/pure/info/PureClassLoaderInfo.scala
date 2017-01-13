package org.scaladebugger.api.profiles.pure.info


import com.sun.jdi._
import org.scaladebugger.api.lowlevel.{InvokeNonVirtualArgument, InvokeSingleThreadedArgument, JDIArgument}
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._

/**
 * Represents a pure implementation of a class loader profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            class loader
 * @param infoProducer The producer of info-based profile instances
 * @param _classLoaderReference The reference to the underlying JDI class loader
 * @param _virtualMachine The virtual machine associated with the class loader
 * @param _referenceType The reference type for this class loader
 */
class PureClassLoaderInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val _classLoaderReference: ClassLoaderReference
)(
  override protected val _virtualMachine: VirtualMachine = _classLoaderReference.virtualMachine(),
  private val _referenceType: ReferenceType = _classLoaderReference.referenceType()
) extends PureObjectInfo(scalaVirtualMachine, infoProducer, _classLoaderReference)(
  _virtualMachine = _virtualMachine,
  _referenceType = _referenceType
) with ClassLoaderInfo {
  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = true

  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: ClassLoaderInfo = {
    infoProducer.toJavaInfo.newClassLoaderInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      classLoaderReference = _classLoaderReference
    )(
      virtualMachine = _virtualMachine,
      referenceType = _referenceType
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ClassLoaderReference = _classLoaderReference

  /**
   * Retrieves all loaded classes defined by this class loader.
   *
   * @return The collection of reference types for the loaded classes
   */
  override def definedClasses: Seq[ReferenceTypeInfo] = {
    import scala.collection.JavaConverters._
    _classLoaderReference.definedClasses().asScala
      .map(newTypeProfile).map(_.toReferenceType)
  }

  /**
   * Retrieves all classes for which this class loader served as the initiating
   * loader.
   *
   * @return The collection of reference types for the initiated classes
   */
  override def visibleClasses: Seq[ReferenceTypeInfo] = {
    import scala.collection.JavaConverters._
    _classLoaderReference.visibleClasses().asScala
      .map(newTypeProfile).map(_.toReferenceType)
  }
}
