// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `sbt-fresh` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin, SbtPlugin)
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
      val sbtGit = "1.0.0"
    }
    val sbtGit = "com.typesafe.sbt" % "sbt-git" % Version.sbtGit
  }


// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  scalafmtSettings ++
  sbtScriptedSettings

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.3",
    organization := "de.heikoseeberger",
    organizationName := "Heiko Seeberger",
    startYear := Some(2016),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-Ypartial-unification",
      "-Ywarn-unused-import"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    publishMavenStyle := false
)

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val sbtScriptedSettings =
  Seq(
    scriptedLaunchOpts ++= Seq(
      "-Xmx1024M",
      s"-Dplugin.version=${version.value}"
    )
  )
