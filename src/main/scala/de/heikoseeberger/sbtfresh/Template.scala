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

import java.time.LocalDate.now

private object Template {

  private val year = now().getYear

  def buildProperties: String =
    """|sbt.version = 1.6.2
       |""".stripMargin

  def buildSbt(
      organization: String,
      name: String,
      author: String,
      license: Option[License],
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

    s"""|// *****************************************************************************
        |// Build settings
        |// *****************************************************************************
        |
        |inThisBuild(
        |  Seq(
        |    organization := "$organization",
        |    organizationName := "$author",
        |    startYear := Some($year),$licenseSettings
        |    scalaVersion := "3.1.0",
        |    scalacOptions ++= Seq(
        |      "-deprecation",
        |      "-unchecked",
        |      "-rewrite",
        |      "-indent",
        |      "-pagewidth",
        |      "100",
        |      "-source",
        |      "future",
        |      "-Xfatal-warnings",
        |    ),
        |    testFrameworks += new TestFramework("munit.Framework"),
        |    scalafmtOnCompile := true,
        |    dynverSeparator := "_", // the default `+` is not compatible with docker tags
        |  )
        |)
        |
        |// *****************************************************************************
        |// Projects
        |// *****************************************************************************
        |
        |lazy val $nameIdentifier =
        |  project
        |    .in(file("."))
        |    .enablePlugins(AutomateHeaderPlugin)
        |    .settings(commonSettings)
        |    .settings(
        |      libraryDependencies ++= Seq(
        |        library.munit           % Test,
        |        library.munitScalaCheck % Test,
        |      ),
        |    )
        |
        |// *****************************************************************************
        |// Project settings
        |// *****************************************************************************
        |
        |lazy val commonSettings =
        |  Seq(
        |    // Also (automatically) format build definition together with sources
        |    Compile / scalafmt := {
        |      val _ = (Compile / scalafmtSbt).value
        |      (Compile / scalafmt).value
        |    },
        |  )
        |
        |// *****************************************************************************
        |// Library dependencies
        |// *****************************************************************************
        |
        |lazy val library =
        |  new {
        |    object Version {
        |      val munit = "0.7.29"
        |    }
        |    val munit           = "org.scalameta" %% "munit"            % Version.munit
        |    val munitScalaCheck = "org.scalameta" %% "munit-scalacheck" % Version.munit
        |  }
        |""".stripMargin
  }

  def gitignore: String =
    """|# sbt
       |.bsp
       |lib_managed
       |project/project
       |target
       |
       |# Worksheets
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
       |.ensime_cache
       |.ensime_lucene
       |
       |# VSCode
       |.vscode
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
       |.bloop
       |.metals
       |metals.sbt
       |""".stripMargin

  def notice(author: String): String =
    s"""|Copyright $year $author
        |""".stripMargin

  def plugins: String =
    s"""|addSbtPlugin("com.dwijnand"      % "sbt-dynver"   % "4.1.1")
        |addSbtPlugin("de.heikoseeberger" % "sbt-header"   % "5.6.5")
        |addSbtPlugin("org.scalameta"     % "sbt-scalafmt" % "2.4.6")
        |""".stripMargin

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

  def scalafixConf: String =
    """|OrganizeImports {
       |  blankLines = Manual
       |  coalesceToWildcardImportThreshold = null
       |  expandRelative = false
       |  groupExplicitlyImportedImplicitsSeparately = false
       |  groupedImports = AggressiveMerge
       |  groups = [ "*" ]
       |  importSelectorsOrder = Ascii
       |  importsOrder = Ascii
       |  preset = DEFAULT
       |  removeUnused = true
       |}""".stripMargin

  def scalafmtConf: String =
    """|version = "3.4.3"
       |
       |preset         = "defaultWithAlign"
       |runner.dialect = "scala3"
       |
       |maxColumn                        = 100
       |indentOperator.preset            = "spray"
       |indentOperator.exemptScope       = "all"
       |spaces.inImportCurlyBraces       = true
       |rewrite.rules                    = ["Imports", "RedundantBraces", "RedundantParens"]
       |rewrite.imports.sort             = "ascii"
       |docstrings.blankFirstLine        = true
       |trailingCommas                   = "preserve"
       |newlines.beforeCurlyLambdaParams = "multilineWithCaseOnly"
       |""".stripMargin
}
