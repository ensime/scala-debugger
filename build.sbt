//
// DEBUGGER API PROJECT CONFIGURATION
//
lazy val scalaDebuggerApi = project
  .in(file("scala-debugger-api"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(Api.settings: _*)
  .settings(name := "scala-debugger-api")
  .dependsOn(scalaDebuggerMacros % "compile->compile;test->compile;it->compile")
  .dependsOn(scalaDebuggerTest % "test->compile;it->compile;test->test;it->test")

//
// DEBUGGER TEST CODE PROJECT CONFIGURATION
//
lazy val scalaDebuggerTest = project
  .in(file("scala-debugger-test"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(DebuggerTest.settings: _*)
  .settings(
    // Do not publish the test project
    publishArtifact := false,
    publishLocal := {}
  )

//
// DEBUGGER MACRO PROJECT CONFIGURATION
//
lazy val scalaDebuggerMacros = project
  .in(file("scala-debugger-macros"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(Macros.settings: _*)
  .settings(name := "scala-debugger-macros")

//
// DEBUGGER DOC PROJECT CONFIGURATION
//
lazy val scalaDebuggerDocs = project
  .in(file("scala-debugger-docs"))
  .settings(Common.settings: _*)
  .settings(Docs.settings: _*)
  .settings(name := "scala-debugger-docs")

//
// LANGUAGE PROJECT CONFIGURATION
//
lazy val scalaDebuggerLanguage = project
  .in(file("scala-debugger-language"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(Language.settings: _*)
  .settings(Macros.settings: _*)
  .settings(name := "scala-debugger-language")
  .dependsOn(scalaDebuggerApi % "compile->compile;test->compile;it->compile")

//
// DEBUGGER TOOL PROJECT CONFIGURATION
//
lazy val scalaDebuggerTool = project
  .in(file("scala-debugger-tool"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(Tool.settings: _*)
  .settings(name := "scala-debugger-tool")
  .dependsOn(scalaDebuggerApi % "compile->compile;test->compile;it->compile")
  .dependsOn(scalaDebuggerLanguage % "compile->compile;test->compile;it->compile")
  .dependsOn(scalaDebuggerTest % "test->compile;it->compile;test->test;it->test")

//
// SBT SCALA DEBUGGER PLUGIN
//
lazy val sbtScalaDebuggerPlugin = project
  .in(file("sbt-scala-debugger-plugin"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(SbtPlugin.settings: _*)
  .settings(name := "sbt-scala-debugger")
  .enablePlugins(CrossPerProjectPlugin)

//
// MAIN PROJECT CONFIGURATION
//
lazy val root = project
  .in(file("."))
  .settings(Common.settings: _*)
  .settings(
    name := "scala-debugger",
    // Do not publish the aggregation project
    publishArtifact := false,
    publishLocal := {}
  ).aggregate(
    scalaDebuggerApi,
    scalaDebuggerTest,
    scalaDebuggerMacros,
    scalaDebuggerLanguage,
    scalaDebuggerTool,
    sbtScalaDebuggerPlugin
  ).enablePlugins(CrossPerProjectPlugin)

