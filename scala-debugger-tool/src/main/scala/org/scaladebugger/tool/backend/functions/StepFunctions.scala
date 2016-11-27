package org.scaladebugger.tool.backend.functions
import acyclic.file
import com.sun.jdi.ThreadReference
import com.sun.jdi.event.StepEvent
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.tool.backend.StateManager

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import org.scaladebugger.api.dsl.Implicits._
import org.scaladebugger.api.profiles.traits.info.ThreadInfoProfile
import org.scaladebugger.api.profiles.traits.info.events.StepEventInfoProfile

/**
 * Represents a collection of functions for managing steps.
 *
 * @param stateManager The manager whose state to share among functions
 * @param writeLine Used to write output to the terminal
 */
class StepFunctions(
  private val stateManager: StateManager,
  private val writeLine: String => Unit
) {
  // TODO: Enable CLI configuration of this property
  private val MaxWaitDuration = 15.seconds

  /** Entrypoint for stepping into a line. */
  def stepIntoLine(m: Map[String, Any]) = {
    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    val thread = stateManager.state.activeThread
    if (thread.isEmpty) writeLine("No active thread!")

    thread.foreach(t =>
      // TODO: Handle using on JVM of active thread
      jvms.foreach(s =>
        performStep(t, s.stepIntoLine)
      )
    )
  }

  /** Entrypoint for stepping into using min size. */
  def stepIntoMin(m: Map[String, Any]) = {
    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    val thread = stateManager.state.activeThread
    if (thread.isEmpty) writeLine("No active thread!")

    thread.foreach(t =>
      // TODO: Handle using on JVM of active thread
      jvms.foreach(s =>
        performStep(t, s.stepIntoMin)
      )
    )
  }

  /** Entrypoint for stepping over a line. */
  def stepOverLine(m: Map[String, Any]) = {
    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    val thread = stateManager.state.activeThread
    if (thread.isEmpty) writeLine("No active thread!")

    thread.foreach(t =>
      // TODO: Handle using on JVM of active thread
      jvms.foreach(s =>
        performStep(t, s.stepOverLine)
      )
    )
  }

  /** Entrypoint for stepping over using min size. */
  def stepOverMin(m: Map[String, Any]) = {
    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    val thread = stateManager.state.activeThread
    if (thread.isEmpty) writeLine("No active thread!")

    thread.foreach(t =>
      // TODO: Handle using on JVM of active thread
      jvms.foreach(s =>
        performStep(t, s.stepOverMin)
      )
    )
  }

  /** Entrypoint for stepping out of a line. */
  def stepOutLine(m: Map[String, Any]) = {
    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    val thread = stateManager.state.activeThread
    if (thread.isEmpty) writeLine("No active thread!")

    thread.foreach(t =>
      // TODO: Handle using on JVM of active thread
      jvms.foreach(s =>
        performStep(t, s.stepOutLine)
      )
    )
  }

  /** Entrypoint for stepping over using min size. */
  def stepOutMin(m: Map[String, Any]) = {
    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    val thread = stateManager.state.activeThread
    if (thread.isEmpty) writeLine("No active thread!")

    thread.foreach(t =>
      // TODO: Handle using on JVM of active thread
      jvms.foreach(s =>
        performStep(t, s.stepOutMin)
      )
    )
  }

  /**
   * Performs a step by creating a step request, ensuring the that referenced
   * thread is not suspended (so it can process the request), and also
   * indicating that the step event should not resume the thread.
   *
   * @param threadInfo The info about the thread within which to step
   * @param stepFunc The function to create the step request
   */
  private def performStep(
    threadInfo: ThreadInfoProfile,
    stepFunc: (ThreadInfoProfile, Seq[JDIEventArgument]) => Future[StepEventInfoProfile]
  ): Unit = {
    val resumeCount = threadInfo.status.suspendCount

    // Create a step function that does not resume the thread upon completion
    val f = stepFunc(threadInfo, Seq(NoResume))

    f.foreach(e => {
      val l = e.location
      val tn = e.thread.name
      val cn = l.declaringType.name
      val mn = l.method.name
      val sn = l.sourceName
      val ln = l.lineNumber

      writeLine(s"Step completed: 'thread=$tn', $cn.$mn ($sn:$ln)")
    })

    // Remove all suspensions on the thread so it can process the
    // step request
    (1 to resumeCount).foreach(_ => threadInfo.resume())

    Await.ready(f, MaxWaitDuration)
  }
}
