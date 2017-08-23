import sbt.Keys._
import sbt._

import scala.util.Try

object Acyclic {
  def settings: Seq[Setting[_]] = Seq(
    libraryDependencies += "com.lihaoyi" %% "acyclic" % "0.1.7" % "provided",
    libraryDependencies ++= (
      if (scalaVersion.value.startsWith("2.10")) Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
      ) else Nil
    ),
    autoCompilerPlugins := true,
    addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.7")
  )
}

