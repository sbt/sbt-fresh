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
  "org.eclipse.jgit" % "org.eclipse.jgit" % "4.4.1.201607150455-r"
)

initialCommands := """|import de.heikoseeberger.sbtfresh._""".stripMargin

sbtPlugin         := true
publishMavenStyle := false

scriptedSettings
scriptedLaunchOpts := scriptedLaunchOpts.value ++ Vector(
  "-Xmx1024M",
  s"-Dplugin.version=${version.value}"
)

git.useGitDescribe := true

import scalariform.formatter.preferences._
scalariformPreferences := scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)

headers := Map("scala" -> de.heikoseeberger.sbtheader.license.Apache2_0("2016", "Heiko Seeberger"))
