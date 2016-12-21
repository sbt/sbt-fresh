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
    """|sbt.version = 0.13.13
       |""".stripMargin

  def buildSbt(organization: String,
               name: String,
               packageSegments: Vector[String],
               author: String,
               license: Option[License]): String = {
    val nameIdentifier =
      if (name.segments.mkString == name) name else s"`$name`"

    val licenseSettings = {
      def settings(license: License) = {
        val License(_, name, url, _) = license
        s"""|
            |    licenses += ("$name",
            |                 url("$url")),
            |    mappings.in(Compile, packageBin) +=
            |      baseDirectory.in(ThisBuild).value / "LICENSE" -> "LICENSE",""".stripMargin
      }
      license.map(settings).getOrElse("")
    }

    val headerSettings = {
      def settings(license: License) =
        s"""headers := Map("scala" -> ${license.headerName}("$year", "$author"))"""
      def fallback =
        s"""|headers := Map(
            |      "scala" -> (HeaderPattern.cStyleBlockComment,
            |                  $TQ|/*
            ||                     | * Copyright year author
            ||                     | */$TQ.stripMargin)
            |    )""".stripMargin
      license.fold(fallback)(settings)
    }

    s"""|// *****************************************************************************
        |// Projects
        |// *****************************************************************************
        |
        |lazy val $nameIdentifier =
        |  project
        |    .in(file("."))
        |    .enablePlugins(AutomateHeaderPlugin, GitVersioning)
        |    .settings(settings)
        |    .settings(
        |      libraryDependencies ++= Seq(
        |        library.scalaCheck % Test,
        |        library.scalaTest  % Test
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
        |      val scalaCheck = "1.13.4"
        |      val scalaTest  = "3.0.1"
        |    }
        |    val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
        |    val scalaTest  = "org.scalatest"  %% "scalatest"  % Version.scalaTest
        |}
        |
        |// *****************************************************************************
        |// Settings
        |// *****************************************************************************        |
        |
        |lazy val settings =
        |  commonSettings ++
        |  scalafmtSettings ++
        |  gitSettings ++
        |  headerSettings
        |
        |lazy val commonSettings =
        |  Seq(
        |    scalaVersion := "2.12.1",
        |    crossScalaVersions := Seq(scalaVersion.value, "2.11.8"),
        |    organization := "$organization",$licenseSettings
        |    scalacOptions ++= Seq(
        |      "-unchecked",
        |      "-deprecation",
        |      "-language:_",
        |      "-target:jvm-1.8",
        |      "-encoding", "UTF-8"
        |    ),
        |    javacOptions ++= Seq(
        |      "-source", "1.8",
        |      "-target", "1.8"
        |    ),
        |    unmanagedSourceDirectories.in(Compile) :=
        |      Seq(scalaSource.in(Compile).value),
        |    unmanagedSourceDirectories.in(Test) :=
        |      Seq(scalaSource.in(Test).value)
        |)
        |
        |lazy val scalafmtSettings =
        |  reformatOnCompileSettings ++
        |  Seq(
        |    formatSbtFiles := false,
        |    scalafmtConfig :=
        |      Some(baseDirectory.in(ThisBuild).value / ".scalafmt.conf"),
        |    ivyScala :=
        |      ivyScala.value.map(_.copy(overrideScalaVersion = sbtPlugin.value)) // TODO Remove once this workaround no longer needed (https://github.com/sbt/sbt/issues/2786)!
        |  )
        |
        |lazy val gitSettings =
        |  Seq(
        |    git.useGitDescribe := true
        |  )
        |
        |import de.heikoseeberger.sbtheader.HeaderPattern
        |import de.heikoseeberger.sbtheader.license._
        |lazy val headerSettings =
        |  Seq(
        |    $headerSettings
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
       |# Akka Persistence
       |journal
       |snapshots
       |
       |# Log files
       |*.log
       |""".stripMargin

  def notice(author: String): String =
    s"""|Copyright $year $author
        |""".stripMargin

  def `package`(packageSegments: Vector[String], author: String): String = {
    val superPackage = packageSegments.init.mkString(".")
    val lastSegment  = packageSegments.last
    s"""|
        |package $superPackage
        |
        |package object $lastSegment {
        |
        |  type Traversable[+A] = scala.collection.immutable.Traversable[A]
        |  type Iterable[+A]    = scala.collection.immutable.Iterable[A]
        |  type Seq[+A]         = scala.collection.immutable.Seq[A]
        |  type IndexedSeq[+A]  = scala.collection.immutable.IndexedSeq[A]
        |}
        |""".stripMargin
  }

  def plugins: String =
    """|addSbtPlugin("com.geirsson"      % "sbt-scalafmt" % "0.4.10")
       |addSbtPlugin("com.typesafe.sbt"  % "sbt-git"      % "0.8.5")
       |addSbtPlugin("de.heikoseeberger" % "sbt-header"   % "1.6.0")
       |""".stripMargin

  def readme(name: String, license: Option[License]): String = {
    val licenseText = {
      def text(license: License) = {
        val License(_, name, url, _) = license
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
        |Contributions via GitHub pull requests are gladly accepted from their original
        |author. Along with any pull requests, please state that the contribution is your
        |original work and that you license the work to the project under the project's
        |open source license. Whether or not you state this explicitly, by submitting any
        |copyrighted material via pull request, email, or other means you agree to
        |license the material under the project's open source license and warrant that
        |you have the legal authority to do so.
        |
        |$licenseText
        |""".stripMargin
  }

  def scalafmtConf: String =
    """|style = defaultWithAlign
       |
       |danglingParentheses        = true
       |indentOperator             = spray
       |rewrite.rules              = [RedundantBraces, RedundantParens, SortImports]
       |spaces.inImportCurlyBraces = true
       |unindentTopLevelOperators  = true
       |""".stripMargin

  def shellPrompt: String =
    """|shellPrompt.in(ThisBuild) := { state =>
       |  val project = Project.extract(state).currentRef.project
       |  s"[$project]> "
       |}
       |""".stripMargin
}
