import UnidocKeys._

//
// DEBUGGER API PROJECT CONFIGURATION
//
lazy val scalaDebuggerApi = project
  .in(file("scala-debugger-api"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Acyclic.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(Api.settings: _*)
  .settings(name := "scala-debugger-api")
  .dependsOn(scalaDebuggerMacros % "compile->compile;test->compile;it->compile")
  .dependsOn(scalaDebuggerTest % "test->compile;it->compile")

//
// DEBUGGER TEST CODE PROJECT CONFIGURATION
//
lazy val scalaDebuggerTest = project
  .in(file("scala-debugger-test"))
  .settings(Common.settings: _*)
  .settings(Acyclic.settings: _*)
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
  .settings(Common.settings: _*)
  .settings(Acyclic.settings: _*)
  .settings(Macros.settings: _*)
  .settings(name := "scala-debugger-macros")

//
// LANGUAGE REPL PROJECT CONFIGURATION
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
// DEBUGGER REPL PROJECT CONFIGURATION
//
lazy val scalaDebuggerRepl = project
  .in(file("scala-debugger-repl"))
  .configs(IntegrationTest)
  .settings(Common.settings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(Repl.settings: _*)
  .settings(name := "scala-debugger-repl")
  .dependsOn(scalaDebuggerApi % "compile->compile;test->compile;it->compile")
  .dependsOn(scalaDebuggerLanguage % "compile->compile;test->compile;it->compile")

//
// MAIN PROJECT CONFIGURATION
//
lazy val root = project
  .in(file("."))
  .settings(Common.settings: _*)
  .settings(Acyclic.settings: _*)
  .settings(unidocSettings: _*)
  .settings(
    name := "scala-debugger",
    // Do not publish the aggregation project
    publishArtifact := false,
    publishLocal := {},
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(
      scalaDebuggerTest
    )
  ).aggregate(
    scalaDebuggerApi,
    scalaDebuggerTest,
    scalaDebuggerMacros,
    scalaDebuggerLanguage,
    scalaDebuggerRepl
  )

