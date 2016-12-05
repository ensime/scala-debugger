package org.scaladebugger.api.lowlevel.events.misc

import org.scaladebugger.api.lowlevel.events.misc.processors.ResumeProcessor
import org.scaladebugger.api.lowlevel.events.{JDIEventProcessor, JDIEventArgument}

/**
 * Represents a flag to resume or not resume upon receiving an event.
 */
sealed trait Resume extends JDIEventArgument {
  /** True if should resume, otherwise false. */
  val value: Boolean

  override def toProcessor: ResumeProcessor = new ResumeProcessor(this)
}

/**
 * Represents a flag indicating to not resume from an event.
 */
case object NoResume extends Resume { val value: Boolean = false }

/**
 * Represents a flag indicating to resume from an event.
 */
case object YesResume extends Resume { val value: Boolean = true }
