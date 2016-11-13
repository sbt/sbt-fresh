val `sbt-fresh` =
  project
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)

scalacOptions ++= Vector(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.6",
  "-encoding", "UTF-8"
)
unmanagedSourceDirectories.in(Compile) := Vector(scalaSource.in(Compile).value)
unmanagedSourceDirectories.in(Test)    := Vector(scalaSource.in(Test).value)

organization := "de.heikoseeberger"
licenses     += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

sbtPlugin         := true
publishMavenStyle := false

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "4.5.0.201609210915-r"

git.useGitDescribe := true

import scalariform.formatter.preferences._
scalariformPreferences := scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)

import de.heikoseeberger.sbtheader.license._
headers := Map("scala" -> Apache2_0("2016", "Heiko Seeberger"))

scriptedSettings
scriptedLaunchOpts ++= Vector(
  "-Xmx1024M",
  s"-Dplugin.version=${version.value}"
)
