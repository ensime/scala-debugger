package org.scaladebugger.docs

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Main entrypoint for generating and serving docs.
 */
object Main {
  /** Logger for this class. */
  private lazy val logger = new Logger(this.getClass)

  def main(args: Array[String]): Unit = {
    val config = new Config(args)

    // Set global logger used throughout program
    Logger.setDefaultLevel(config.defaultLogLevel())

    // Generate before other actions if indicated
    if (config.generate()) {
      new Generator(config).run()
    }

    // Serve generated content
    if (config.serve()) {
      val rootPath = Paths.get(config.inputDir())

      val watcherThread = if (config.liveReload()) {
        logger.log(s"Watching $rootPath for changes")
        val watcher = new Watcher(
          path = rootPath,
          callback = (rootPath, events) => {
            logger.verbose(s"Detected ${events.length} change(s) at $rootPath")
            new Generator(config).run()
          },
          waitTime = config.liveReloadWaitTime(),
          waitUnit = TimeUnit.MILLISECONDS
        )

        Some(watcher.runAsync())
      } else None

      new Server(config).run()

      watcherThread.foreach(t => {
        logger.verbose("Shutting down watcher thread")
        t.interrupt()
      })

    // Publish generated content
    } else if (config.publish()) {
      logger.fatal("TODO: Implement publish using Scala process to run git")
      logger.info("Needs to switch to the gh-pages branch of the project")
      logger.info("Needs to clear the old contents of the gh-pages branch")
      logger.info("Needs to generate a .nojekyll file")
      logger.info("Needs to generate a sitemap.xml file")
      logger.info("Needs to generate normal output")
      logger.info("Needs to copy output into gh-pages directory")
      logger.info("Needs to create a commit and push")
      logger.info("Needs to switch back to the previous branch")

    // Print help info
    } else if (!config.generate()) {
      config.printHelp()
    }
  }
}
