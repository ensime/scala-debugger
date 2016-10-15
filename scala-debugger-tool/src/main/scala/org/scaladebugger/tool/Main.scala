package org.scaladebugger.tool
import acyclic.file

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.tool.backend.ArgumentParser

object Main {
  def main(args: Array[String]): Unit = {
    val results = ArgumentParser.parse(args)

    // Attempt to load the JDI into our system
    if (!JDITools.tryLoadJdi()) return

    if (results.help) {
      Console.out.println("No help available.")
    } else {
      val repl = Repl.newInstance(forceUseFallback = results.forceUseFallback)

      // Start REPL and wait for completion
      repl.start()
      while (repl.isRunning) Thread.sleep(1000)

      // Clear state, which will shutdown active debugger and allow proper exit
      repl.stop()
    }
  }
}
