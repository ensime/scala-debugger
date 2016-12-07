package org.scaladebugger.api.profiles.swappable.info
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.info.{MiscInfo, ValueInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a swappable profile for miscellaneous info that redirects the
 * invocation to another profile.
 */
trait SwappableMiscInfo extends MiscInfo {
  this: SwappableDebugProfileManagement =>

  override def toScalaVirtualMachine: ScalaVirtualMachine = {
    withCurrentProfile.toScalaVirtualMachine
  }

  override def availableLinesForFile(fileName: String): Option[Seq[Int]] = {
    withCurrentProfile.availableLinesForFile(fileName)
  }

  override def sourceNameToPaths(sourceName: String): Seq[String] = {
    withCurrentProfile.sourceNameToPaths(sourceName)
  }

  override def commandLineArguments: Seq[String] = {
    withCurrentProfile.commandLineArguments
  }

  override def mainClassName: String = {
    withCurrentProfile.mainClassName
  }
}
