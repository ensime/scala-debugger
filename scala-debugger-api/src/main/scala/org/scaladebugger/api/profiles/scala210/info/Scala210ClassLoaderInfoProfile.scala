package org.scaladebugger.api.profiles.scala210.info

//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.pure.info.PureClassLoaderInfoProfile
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a scala210 implementation of a class loader profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            class loader
 * @param _classLoaderReference The reference to the underlying JDI class loader
 * @param _virtualMachine The virtual machine associated with the class loader
 * @param _threadReference The thread associated with the class loader
 *                        (for method invocation)
 * @param _referenceType The reference type for this class loader
 */
class Scala210ClassLoaderInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _classLoaderReference: ClassLoaderReference
)(
  override protected val _virtualMachine: VirtualMachine = _classLoaderReference.virtualMachine(),
  private val _threadReference: ThreadReference = _classLoaderReference.owningThread(),
  private val _referenceType: ReferenceType = _classLoaderReference.referenceType()
) extends PureClassLoaderInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  _classLoaderReference = _classLoaderReference
)(
  _virtualMachine = _virtualMachine,
  _threadReference = _threadReference,
  _referenceType = _referenceType
) {
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
  override def definedClasses: Seq[ReferenceTypeInfoProfile] = {
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
  override def visibleClasses: Seq[ReferenceTypeInfoProfile] = {
    import scala.collection.JavaConverters._
    _classLoaderReference.visibleClasses().asScala
      .map(newTypeProfile).map(_.toReferenceType)
  }
}
