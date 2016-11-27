package org.scaladebugger.api.dsl.classes

import com.sun.jdi.event.ClassUnloadEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.classes.ClassUnloadProfile
import org.scaladebugger.api.profiles.traits.info.events.ClassUnloadEventInfoProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param classUnloadProfile The profile to wrap
 */
class ClassUnloadDSLWrapper private[dsl] (
  private val classUnloadProfile: ClassUnloadProfile
) {
  /** Represents a ClassUnload event and any associated data. */
  type ClassUnloadEventAndData = (ClassUnloadEventInfoProfile, Seq[JDIEventDataResult])

  /** @see ClassUnloadProfile#tryGetOrCreateClassUnloadRequest(JDIArgument*) */
  def onClassUnload(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassUnloadEventInfoProfile]] =
    classUnloadProfile.tryGetOrCreateClassUnloadRequest(extraArguments: _*)

  /** @see ClassUnloadProfile#getOrCreateClassUnloadRequest(JDIArgument*) */
  def onUnsafeClassUnload(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassUnloadEventInfoProfile] =
    classUnloadProfile.getOrCreateClassUnloadRequest(extraArguments: _*)

  /** @see ClassUnloadProfile#getOrCreateClassUnloadRequestWithData(JDIArgument*) */
  def onUnsafeClassUnloadWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassUnloadEventAndData] =
    classUnloadProfile.getOrCreateClassUnloadRequestWithData(
      extraArguments: _*
    )

  /** @see ClassUnloadProfile#tryGetOrCreateClassUnloadRequestWithData(JDIArgument*) */
  def onClassUnloadWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassUnloadEventAndData]] =
    classUnloadProfile.tryGetOrCreateClassUnloadRequestWithData(
      extraArguments: _*
    )
}
