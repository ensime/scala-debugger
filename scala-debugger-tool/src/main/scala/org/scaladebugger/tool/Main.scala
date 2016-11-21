package org.scaladebugger.tool
import acyclic.file

import org.scaladebugger.api.utils.JDITools

object Main {
  def main(args: Array[String]): Unit = {
    // Parse CLI arguments
    val config = new Config(args)

    // Attempt to load the JDI into our system
    if (!JDITools.tryLoadJdi()) return

    // Create new repl instance and use provided settings
    val repl = Repl.newInstance(config = config)

    // Start REPL and wait for completion
    repl.start()
    while (repl.isRunning) Thread.sleep(1000)

    // Clear state, which will shutdown active debugger and allow proper exit
    repl.stop()
  }
}
