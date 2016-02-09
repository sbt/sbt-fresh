val sbtFresh = project
  .copy(id = "sbt-fresh")
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)

organization := "de.heikoseeberger"
name         := "sbt-fresh"
licenses     += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalacOptions ++= Vector(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.6",
  "-encoding", "UTF-8"
)

unmanagedSourceDirectories.in(Compile) := Vector(scalaSource.in(Compile).value)
unmanagedSourceDirectories.in(Test)    := Vector(scalaSource.in(Test).value)

libraryDependencies ++= Vector(
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
  "org.scalatest"  %% "scalatest"  % "2.2.6"  % "test"
)

initialCommands := """|import de.heikoseeberger.sbtfresh._""".stripMargin

sbtPlugin         := true
publishMavenStyle := false

git.useGitDescribe := true

import scalariform.formatter.preferences._
scalariformPreferences := scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)

headers := Map("scala" -> de.heikoseeberger.sbtheader.license.Apache2_0("2016", "Heiko Seeberger"))
