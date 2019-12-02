/*
 * Copyright 2016 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.heikoseeberger.sbtfresh

import de.heikoseeberger.sbtfresh.license.License
import java.time.LocalDate.now

private object Template {

  private val year = now().getYear

  def buildProperties: String =
    """|sbt.version = 1.3.4
       |""".stripMargin

  def buildSbt(
      organization: String,
      name: String,
      packageSegments: Vector[String],
      author: String,
      license: Option[License],
      setUpTravis: Boolean,
      setUpWartremover: Boolean
  ): String = {
    val nameIdentifier = if (name.segments.mkString == name) name else s"`$name`"

    val licenseSettings = {
      def settings(license: License) = {
        val License(_, name, url) = license
        s"""|
            |    licenses += ("$name", url("$url")),""".stripMargin
      }
      license.map(settings).getOrElse("")
    }

    val wartremoverSettings =
      if (setUpWartremover)
        """|,
           |    Compile / compile / wartremoverWarnings ++= Warts.unsafe""".stripMargin
      else
        ""

    val scalaVersion =
      if (setUpTravis)
        """|// scalaVersion from .travis.yml via sbt-travisci
           |    // scalaVersion := "2.13.1",""".stripMargin
      else
        """scalaVersion := "2.13.1","""

    s"""|// *****************************************************************************
        |// Projects
        |// *****************************************************************************
        |
        |lazy val $nameIdentifier =
        |  project
        |    .in(file("."))
        |    .enablePlugins(AutomateHeaderPlugin)
        |    .settings(settings)
        |    .settings(
        |      libraryDependencies ++= Seq(
        |        library.scalaCheck % Test,
        |        library.scalaTest  % Test,
        |      )
        |    )
        |
        |// *****************************************************************************
        |// Library dependencies
        |// *****************************************************************************
        |
        |lazy val library =
        |  new {
        |    object Version {
        |      val scalaCheck = "1.14.2"
        |      val scalaTest  = "3.1.0"
        |    }
        |    val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
        |    val scalaTest  = "org.scalatest"  %% "scalatest"  % Version.scalaTest
        |  }
        |
        |// *****************************************************************************
        |// Settings
        |// *****************************************************************************
        |
        |lazy val settings =
        |  commonSettings ++
        |  scalafmtSettings
        |
        |lazy val commonSettings =
        |  Seq(
        |    $scalaVersion
        |    organization := "$organization",
        |    organizationName := "$author",
        |    startYear := Some($year),$licenseSettings
        |    scalacOptions ++= Seq(
        |      "-unchecked",
        |      "-deprecation",
        |      "-language:_",
        |      "-target:jvm-1.8",
        |      "-encoding", "UTF-8",
        |      "-Ywarn-unused:imports",
        |    ),
        |    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
        |    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value)$wartremoverSettings,
        |)
        |
        |lazy val scalafmtSettings =
        |  Seq(
        |    scalafmtOnCompile := true,
        |  )
        |""".stripMargin
  }

  def gitignore: String =
    """|# sbt
       |lib_managed
       |project/project
       |target
       |
       |# Worksheets (Eclipse or IntelliJ)
       |*.sc
       |
       |# Eclipse
       |.cache*
       |.classpath
       |.project
       |.scala_dependencies
       |.settings
       |.target
       |.worksheet
       |
       |# IntelliJ
       |.idea
       |
       |# ENSIME
       |.ensime
       |.ensime_lucene
       |.ensime_cache
       |
       |# Mac
       |.DS_Store
       |
       |# Akka
       |ddata*
       |journal
       |snapshots
       |
       |# Log files
       |*.log
       |
       |# jenv
       |.java-version
       |
       |# Metals
       |.metals
       |.bloop
       |""".stripMargin

  def notice(author: String): String =
    s"""|Copyright $year $author
        |""".stripMargin

  def plugins(setUpTravis: Boolean, setUpWartremover: Boolean): String = {
    val travisPlugin =
      if (setUpTravis)
        """|
           |addSbtPlugin("com.dwijnand"      % "sbt-travisci"    % "1.2.0")""".stripMargin
      else
        ""
    val wartRemoverPlugin =
      if (setUpWartremover)
        """|
           |addSbtPlugin("org.wartremover"   % "sbt-wartremover" % "2.4.3")""".stripMargin
      else
        ""

    s"""|addSbtPlugin("com.dwijnand"      % "sbt-dynver"      % "4.0.0")${travisPlugin}
        |addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "5.3.1")
        |addSbtPlugin("org.scalameta"     % "sbt-scalafmt"    % "2.2.1")${wartRemoverPlugin}
        |""".stripMargin
  }

  def readme(name: String, license: Option[License]): String = {
    val licenseText = {
      def text(license: License) = {
        val License(_, name, url) = license
        s"""|## License ##
            |
            |This code is open source software licensed under the
            |[$name]($url) license.""".stripMargin
      }
      license.map(text).getOrElse("")
    }
    s"""|# $name #
        |
        |Welcome to $name!
        |
        |## Contribution policy ##
        |
        |Contributions via GitHub pull requests are gladly accepted from their original author. Along with
        |any pull requests, please state that the contribution is your original work and that you license
        |the work to the project under the project's open source license. Whether or not you state this
        |explicitly, by submitting any copyrighted material via pull request, email, or other means you
        |agree to license the material under the project's open source license and warrant that you have the
        |legal authority to do so.
        |
        |$licenseText
        |""".stripMargin
  }

  def scalafmtConf: String =
    """|version = "2.2.1"
       |
       |style = "defaultWithAlign"
       |
       |danglingParentheses               = true
       |indentOperator                    = "spray"
       |maxColumn                         = 100
       |newlines.alwaysBeforeMultilineDef = true
       |project.excludeFilters            = [".*\\.sbt"]
       |rewrite.rules                     = ["AsciiSortImports", "RedundantBraces", "RedundantParens"]
       |spaces.inImportCurlyBraces        = true
       |unindentTopLevelOperators         = true
       |""".stripMargin

  def travisYml: String =
    """|language: scala
       |
       |scala:
       |  - 2.13.1
       |
       |jdk:
       |  - oraclejdk8
       |""".stripMargin
}
