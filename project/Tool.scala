import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._

object Tool {
  /** Tool-specific project settings. */
  val settings = Seq(
    // NOTE: Fork needed to avoid mixing in sbt classloader, which is causing
    //       LinkageError to be thrown for JDI-based classes
    fork in Test := true,
    fork in IntegrationTest := true,

    libraryDependencies ++= Seq(
      "com.lihaoyi" % "ammonite" % "0.7.7" cross CrossVersion.full,
      "com.lihaoyi" % "ammonite-util" % "0.7.7" cross CrossVersion.full,
      "com.lihaoyi" %% "ammonite-terminal" % "0.7.7",
      "org.rogach" %% "scallop" % "2.0.5",
      "org.slf4j" % "slf4j-api" % "1.7.5",
      "org.slf4j" % "slf4j-log4j12" % "1.7.5",
      "log4j" % "log4j" % "1.2.17" % "test,it",
      "org.scalatest" %% "scalatest" % "3.0.0" % "test,it",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.4.2" % "test,it"
    ),

    // Exclude tools.jar (JDI) since not allowed to ship without JDK
    assemblyExcludedJars in assembly := {
      val cp = (fullClasspath in assembly).value
      cp filter {_.data.getName == "tools.jar"}
    }
  )
}
