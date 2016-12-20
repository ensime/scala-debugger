import sbt._
import Keys._

object Macros {
  /** Version used for paradise and quasiquotes. */
  val macroVersion = "2.1.0"

  /** Compiler plugin settings related to macros. */
  val pluginSettings = Seq(
    addCompilerPlugin(
      "org.scalamacros" % "paradise" % macroVersion cross CrossVersion.full
    )
  )

  /** Macro-specific project settings. */
  val settings = pluginSettings ++ Seq(
    libraryDependencies += {
      scalaVersion("org.scala-lang" % "scala-reflect" % _).value
    },

    libraryDependencies ++= (
      if (scalaVersion.value.startsWith("2.10")) Seq(
        "org.scalamacros" %% "quasiquotes" % macroVersion
      ) else Nil
    )
  )
}
