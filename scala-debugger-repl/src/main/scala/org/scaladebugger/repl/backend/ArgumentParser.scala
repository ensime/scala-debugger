package org.scaladebugger.repl.backend
import acyclic.file

/**
 * Created by senkwich on 1/29/16.
 */
object ArgumentParser {
  def parse(args: Seq[String]): ArgumentResults = ArgumentResults(
    forceUseFallback = getBooleanValue(
      args,
      valueWhenProvided = true,
      "--force-use-fallback"
    ),
    help = getBooleanValue(
      args,
      valueWhenProvided = true,
      "--help", "-h"
    )
  )

  private def getBooleanValue(args: Seq[String], valueWhenProvided: Boolean, commands: String*): Boolean = {
    if (commands.exists(c => args.contains(c))) valueWhenProvided
    else !valueWhenProvided
  }

  private def getStringValue(args: Seq[String], defaultValue: String, commands: String*): String = {
    val matches = args.filter(a => commands.exists(a.startsWith))

    matches.lastOption
      .map(_.split('='))
      .filter(_.length > 1)
      .map(_.last)
      .getOrElse(defaultValue)
  }
}
