package org.scaladebugger.api.dsl.classes

import com.sun.jdi.event.ClassUnloadEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.requests.classes.ClassUnloadRequest
import org.scaladebugger.api.profiles.traits.info.events.ClassUnloadEventInfo

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param classUnloadProfile The profile to wrap
 */
class ClassUnloadDSLWrapper private[dsl] (
  private val classUnloadProfile: ClassUnloadRequest
) {
  /** Represents a ClassUnload event and any associated data. */
  type ClassUnloadEventAndData = (ClassUnloadEventInfo, Seq[JDIEventDataResult])

  /** @see ClassUnloadRequest#tryGetOrCreateClassUnloadRequest(JDIArgument*) */
  def onClassUnload(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassUnloadEventInfo]] =
    classUnloadProfile.tryGetOrCreateClassUnloadRequest(extraArguments: _*)

  /** @see ClassUnloadRequest#getOrCreateClassUnloadRequest(JDIArgument*) */
  def onUnsafeClassUnload(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassUnloadEventInfo] =
    classUnloadProfile.getOrCreateClassUnloadRequest(extraArguments: _*)

  /** @see ClassUnloadRequest#getOrCreateClassUnloadRequestWithData(JDIArgument*) */
  def onUnsafeClassUnloadWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassUnloadEventAndData] =
    classUnloadProfile.getOrCreateClassUnloadRequestWithData(
      extraArguments: _*
    )

  /** @see ClassUnloadRequest#tryGetOrCreateClassUnloadRequestWithData(JDIArgument*) */
  def onClassUnloadWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassUnloadEventAndData]] =
    classUnloadProfile.tryGetOrCreateClassUnloadRequestWithData(
      extraArguments: _*
    )
}
