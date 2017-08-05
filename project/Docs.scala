import sbt.Keys._
import sbt._

object Docs {
  /** Docs-specific project settings. */
  val settings = Seq(
    libraryDependencies ++= Seq(
      "org.senkbeil" %% "site-generator" % "0.1.1"
    )
  )
}
