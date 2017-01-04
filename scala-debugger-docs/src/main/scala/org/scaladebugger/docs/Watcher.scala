package org.scaladebugger.docs

import java.nio.file.{FileSystems, Path, StandardWatchEventKinds, WatchEvent}

import scala.language.existentials
import scala.collection.JavaConverters._
import scala.util.Try

object Watcher {
  object EventType extends Enumeration {
    type EventType = Value
    val Create, Modify, Delete = Value

    /**
     * Converts the watch event to an event enumeration.
     *
     * @param watchEvent The watch event to convert
     * @return Some event type if a match for the watch event is found,
     *         otherwise None
     */
    def fromWatchEvent(watchEvent: WatchEvent[_]): Option[EventType] = {
      watchEvent.kind() match {
        case StandardWatchEventKinds.ENTRY_CREATE => Some(Create)
        case StandardWatchEventKinds.ENTRY_MODIFY => Some(Modify)
        case StandardWatchEventKinds.ENTRY_DELETE => Some(Delete)
        case _                                    => None
      }
    }
  }

  import EventType._

  case class Event(
    path: Path,
    `type`: EventType,
    watchEvent: WatchEvent[_]
  )

  object Event {
    /**
     * Converts the watch event to an event.
     *
     * @param watchEvent The watch event to convert
     * @return Some event if a match for the watch event is found,
     *         otherwise None
     */
    def fromWatchEvent(watchEvent: WatchEvent[_]): Option[Event] = {
      EventType.fromWatchEvent(watchEvent).map(eventType => {
        // NOTE: Context should always be a path for our desired events
        val path = watchEvent.context().asInstanceOf[Path]

        Event(
          path = path,
          `type` = eventType,
          watchEvent = watchEvent
        )
      })
    }
  }
}

/**
 * Watches the specified path for changes, calling the notification function
 * when a change occurs.
 *
 * @param path The path to watch
 * @param callback Called when a new watch event is received
 */
class Watcher(
  private val path: Path,
  private val callback: (Watcher.Event) => Unit
) { self =>
  /** Logger for this class. */
  private val logger = new Logger(this.getClass)

  /** Starts the watcher (blocking the current thread). */
  def run(): Unit = {
    val watchService = FileSystems.getDefault.newWatchService()
    path.register(
      watchService,
      StandardWatchEventKinds.ENTRY_CREATE,
      StandardWatchEventKinds.ENTRY_MODIFY,
      StandardWatchEventKinds.ENTRY_DELETE
    )

    while (!Thread.interrupted()) {
      val watchKey = watchService.take()

      println("CHECKING")
      watchKey.pollEvents().asScala
          .map(e => {
            logger.log(s"EVENT: $e")
            e
          })
        .flatMap(Watcher.Event.fromWatchEvent)
        .foreach(e => Try(callback(e)).failed.foreach(logger.error))

      watchKey.reset()

      // Relieve CPU
      Thread.sleep(1)
    }
  }

  /**
   * Starts the watcher in a separate thread.
   *
   * @return The thread running the watcher
   */
  def runAsync(): Thread = {
    val thread = new Thread(new Runnable {
      override def run(): Unit = self.run()
    })

    thread.start()

    thread
  }
}
