import sbt.Keys._
import sbt._

object Repl {
  /** REPL-specific project settings. */
  val settings = Seq(
    libraryDependencies ++= Seq(
      "jline" % "jline" % "2.13"
    )
  )
}
