package org.scaladebugger.docs

import java.nio.file.Paths

/**
 * Main entrypoint for generating and serving docs.
 */
object Main {
  /** Logger for this class. */
  private val logger = new Logger(this.getClass)

  def main(args: Array[String]): Unit = {
    val config = new Config(args)

    // Generate before other actions if indicated
    if (config.generate()) {
      new Generator(config).run()
    }

    // Serve generated content
    if (config.serve()) {
      val rootPath = Paths.get(config.inputDir())

      val watcherThread = if (config.liveReload()) {
        logger.log(s"Watching $rootPath for changes")
        Some(new Watcher(rootPath, e => {
          logger.log(s"Detected change at ${e.path}")
          new Generator(config).run()
        }).runAsync())
      } else None

      new Server(config).run()

      watcherThread.foreach(t => {
        logger.log("Shutting down watcher thread")
        t.interrupt()
      })

    // Publish generated content
    } else if (config.publish()) {
      // TODO: Implement publish using Scala process to run git

    // Print help info
    } else {
      config.printHelp()
    }
  }
}
