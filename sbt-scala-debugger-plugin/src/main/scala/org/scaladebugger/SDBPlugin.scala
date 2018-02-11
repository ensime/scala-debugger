package org.scaladebugger

import sbt.Attributed._
import sbt._
import Keys._
import sbt.Def.Initialize

object SDBPlugin extends AutoPlugin {

  /** Configuration under which SDB is run */
  lazy val ScalaDebugger = config("sdb")

  /** Configuration under which ScalaDebugger is run along with tests */
  lazy val ScalaDebuggerTest = config("sdb-test")


  val scalaDebuggerVersion = settingKey[String]("ScalaDebugger version")


  override def trigger = allRequirements

  override lazy val projectSettings =
    scalaDebuggerSettings(ScalaDebugger, Compile) ++
    scalaDebuggerSettings(ScalaDebuggerTest, Test)

  // Same as Defaults.runTask from sbt, but accepting default arguments too
  def runTask(
    classpath: Initialize[Task[Classpath]],
    mainClassTask: Initialize[Task[Option[String]]],
    scalaRun: Initialize[Task[ScalaRun]],
    defaultArgs: Initialize[Task[Seq[String]]]
  ): Initialize[InputTask[Unit]] = {
    val parser = Def.spaceDelimited()
    Def.inputTask {
      val mainClass = mainClassTask.value getOrElse sys.error("No main class detected.")
      val userArgs = parser.parsed
      val args = if (userArgs.isEmpty) defaultArgs.value else userArgs
      scalaRun.value.run(mainClass, data(classpath.value), args, streams.value.log) foreach sys.error
    }
  }

  def defaultArgs(initialCommands: String): Seq[String] =
    if (initialCommands.isEmpty)
      Nil
    else
      Seq(initialCommands)

  def scalaDebuggerSettings(scalaDebuggerConf: Configuration, underlyingConf: Configuration) = inConfig(scalaDebuggerConf)(
    // Getting references to undefined settings when doing scalaDebugger:run without these
    Defaults.compileSettings ++

    // Seems like the class path provided to scalaDebugger:run doesn't take into account the libraryDependencies below
    // without these
    Classpaths.ivyBaseSettings ++

    Seq(
      scalaDebuggerVersion := {
        val fromEnv = sys.env.get("SDB_VERSION")
        def fromProps = sys.props.get("sdb.version")
        val default = "1.1.0-M3"

        fromEnv
          .orElse(fromProps)
          .getOrElse(default)
      },

      libraryDependencies += "org.scala-debugger" %% "scala-debugger-tool" % scalaDebuggerVersion.value cross CrossVersion.binary,

      // Don't remember under which conditions these two were necessary
//      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value force(),
//      ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },

      configuration := underlyingConf,

      /* Overriding run and runMain defined by compileSettings so that they use fullClasspath of this scope (ScalaDebugger),
       * taking into account the extra libraryDependencies above, and we can also supply default arguments
       * (initialCommands as predef). */
      run := {
        runTask(fullClasspath, mainClass in run, runner in run, (initialCommands in console).map(defaultArgs)).evaluated
      },
      runMain := {
        Defaults.runMainTask(fullClasspath, runner in run).evaluated
      },

      mainClass := Some("org.scaladebugger.tool.Main"),

      /* Required for the input to be provided to ScalaDebugger */
      connectInput := true
    )
  )

}
