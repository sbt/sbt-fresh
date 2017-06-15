// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `sbt-fresh` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin, GitVersioning)
    .settings(settings)
    .settings(
      addSbtPlugin(library.sbtGit)
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val sbtGit = "0.9.3"
    }
    val sbtGit = "com.typesafe.sbt" % "sbt-git" % Version.sbtGit
  }


// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  gitSettings ++
  scalafmtSettings ++
  sbtScriptedSettings

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.1
    organization := "de.heikoseeberger",
    organizationName := "Heiko Seeberger",
    startYear := Some(2016),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.6",
      "-encoding", "UTF-8"
    ),
    unmanagedSourceDirectories.in(Compile) := Seq(scalaSource.in(Compile).value),
    unmanagedSourceDirectories.in(Test) := Seq(scalaSource.in(Test).value),
    shellPrompt in ThisBuild := { state =>
      val project = Project.extract(state).currentRef.project
      s"[$project]> "
    },
    sbtPlugin := true,
    publishMavenStyle := false
)

lazy val gitSettings =
  Seq(
    git.useGitDescribe := true
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true,
    scalafmtVersion := "1.0.0-RC3"
  )

lazy val sbtScriptedSettings =
  scriptedSettings ++
  Seq(
    scriptedLaunchOpts ++= Vector(
      "-Xmx1024M",
      s"-Dplugin.version=${version.value}"
    )
  )
