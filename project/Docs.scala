import sbt.Keys._
import sbt._

object Docs {
  /** Docs-specific project settings. */
  val settings = Seq(
    libraryDependencies ++= Seq(
      "org.senkbeil" %% "grus-layouts" % "0.1.0"
    )
  )
}
