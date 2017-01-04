package org.scaladebugger.docs

import org.scaladebugger.docs.layouts.Layout

/**
 * Main entrypoint for generating and serving docs.
 */
object Main {

  def main(args: Array[String]): Unit = {
    val config = new Config(args)

    // Generate before other actions if indicated
    if (config.generate()) {
      new Generator(config).run()
    }

    // Serve generated content
    if (config.serve()) {
      new Server(config).run()

    // Publish generated content
    } else if (config.publish()) {
      // TODO: Implement publish using Scala process to run git

    // Print help info
    } else {
      config.printHelp()
    }
  }
}
