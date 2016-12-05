package org.scaladebugger.api.lowlevel.events.misc.processors

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.{JDIEventArgument, JDIEventProcessor}
import org.scaladebugger.api.lowlevel.events.filters.{JDIEventFilter, JDIEventFilterProcessor, OrFilter}
import org.scaladebugger.api.lowlevel.events.misc.Resume

/**
 * Represents a processor for resume.
 *
 * @param resume The resume entity to use when processing
 */
class ResumeProcessor(
  val resume: Resume
) extends JDIEventProcessor {
  /**
   * Processes the provided event.
   *
   * @param event Unused
   *
   * @return True if resuming, otherwise false
   */
  override def process(event: Event): Boolean = {
    resume.value
  }

  override val argument: JDIEventArgument = resume
}
