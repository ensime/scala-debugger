package org.scaladebugger.api.profiles.swappable.info
//import acyclic.file
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.info.{MiscInfoProfile, ValueInfoProfile}

/**
 * Represents a swappable profile for miscellaneous info that redirects the
 * invocation to another profile.
 */
trait SwappableMiscInfoProfile extends MiscInfoProfile {
  this: SwappableDebugProfileManagement =>

  override def availableLinesForFile(fileName: String): Option[Seq[Int]] = {
    withCurrentProfile.availableLinesForFile(fileName)
  }

  override def createRemotely(value: AnyVal): ValueInfoProfile = {
    withCurrentProfile.createRemotely(value)
  }

  override def createRemotely(value: String): ValueInfoProfile = {
    withCurrentProfile.createRemotely(value)
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
