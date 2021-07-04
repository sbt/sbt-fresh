// *****************************************************************************
// Build settings
// *****************************************************************************

inThisBuild(
  Seq(
    organization     := "de.heikoseeberger",
    organizationName := "Heiko Seeberger",
    startYear        := Some(2016),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/sbt/sbt-fresh")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/sbt/sbt-fresh"),
        "git@github.com:sbt/sbt-fresh.git"
      )
    ),
    developers := List(
      Developer(
        "hseeberger",
        "Heiko Seeberger",
        "mail@heikoseeberger.rocks",
        url("https://github.com/hseeberger")
      )
    ),
    // scalaVersion defined by sbt
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-encoding",
      "UTF-8",
      "-Ywarn-unused:imports",
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    scalafmtOnCompile := true,
    dynverSeparator   := "_", // the default `+` is not compatible with docker tags
  )
)

// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `sbt-fresh` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin, SbtPlugin)
    .settings(commonSettings)
    .settings(
      addSbtPlugin(library.sbtGit),
      scriptedLaunchOpts ++= Seq(
        "-Xmx1024M",
        s"-Dplugin.version=${version.value}",
      ),
    )

// *****************************************************************************
// Project settings
// *****************************************************************************

lazy val commonSettings =
  Seq(
    // Also (automatically) format build definition together with sources
    Compile / scalafmt := {
      val _ = (Compile / scalafmtSbt).value
      (Compile / scalafmt).value
    },
  )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val sbtGit = "1.0.1",
    }
    val sbtGit = "com.typesafe.sbt" % "sbt-git" % Version.sbtGit
  }
