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
import java.util.Calendar

private object Template {

  private val year = Calendar.getInstance().get(Calendar.YEAR)

  def buildProperties: String =
    """|sbt.version = 0.13.13
       |""".stripMargin

  def buildSbt(organization: String,
               name: String,
               packageSegments: Vector[String]): String = {
    val `package` = packageSegments.mkString(".")
    val n         = if (name.segments.mkString == name) name else s"`$name`"
    s"""|lazy val $n =
        |  project.in(file(".")).enablePlugins(AutomateHeaderPlugin, GitVersioning)
        |
        |libraryDependencies ++= Vector(
        |  Library.scalaTest % "test"
        |)
        |
        |initialCommands := $TQ|import ${`package`}._
        |                      |$TQ.stripMargin
        |""".stripMargin
  }

  def buildScala(organization: String,
                 author: String,
                 license: Option[License]): String = {
    val licenseSettings = {
      def settings(license: License) =
        s"""|
            |      licenses += ${license.sbtSettingValue},
            |      mappings.in(Compile, packageBin)
            |        += baseDirectory.in(ThisBuild).value / "LICENSE" -> "LICENSE",""".stripMargin
      license.map(settings).getOrElse("")
    }

    val headerSettings = {
      def settings(license: License) =
        s"""headers := Map("scala" -> $license("$year", "$author"))"""
      def fallback =
        s"""headers := Map("scala" -> (HeaderPattern.cStyleBlockComment,
      $TQ|/*
         |           | * Copyright $year $author
         |           | */
         |           |$TQ.stripMargin))"""
      license.fold(fallback)(settings)
    }

    s"""|import com.typesafe.sbt.GitPlugin
        |import com.typesafe.sbt.GitPlugin.autoImport._
        |import de.heikoseeberger.sbtheader.HeaderPlugin
        |import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
        |import de.heikoseeberger.sbtheader.HeaderPattern
        |import de.heikoseeberger.sbtheader.license._
        |import org.scalafmt.sbt.ScalaFmtPlugin
        |import org.scalafmt.sbt.ScalaFmtPlugin.autoImport._
        |import sbt._
        |import sbt.Keys._
        |import sbt.plugins.JvmPlugin
        |
        |object Build extends AutoPlugin {
        |
        |  override def requires =
        |    JvmPlugin && HeaderPlugin && GitPlugin && ScalaFmtPlugin
        |
        |  override def trigger = allRequirements
        |
        |  override def projectSettings =
        |    reformatOnCompileSettings ++
        |    Vector(
        |      // Compile settings
        |      scalaVersion := Version.Scala,
        |      crossScalaVersions := Vector(scalaVersion.value),
        |      scalacOptions ++= Vector(
        |        "-unchecked",
        |        "-deprecation",
        |        "-language:_",
        |        "-target:jvm-1.8",
        |        "-encoding", "UTF-8"
        |      ),
        |      unmanagedSourceDirectories.in(Compile) :=
        |        Vector(scalaSource.in(Compile).value),
        |      unmanagedSourceDirectories.in(Test) :=
        |        Vector(scalaSource.in(Test).value),
        |
        |      // Publish settings
        |      organization := "$organization",$licenseSettings
        |
        |      // scalafmt settings
        |      formatSbtFiles := false,
        |      scalafmtConfig := Some(baseDirectory.in(ThisBuild).value / ".scalafmt.conf"),
        |      ivyScala := ivyScala.value.map(_.copy(overrideScalaVersion = sbtPlugin.value)), // TODO Remove once this workaround no longer needed (https://github.com/sbt/sbt/issues/2786)!
        |
        |      // Git settings
        |      git.useGitDescribe := true,
        |
        |      // Header settings
        |      $headerSettings
        |    )
        |}
        |""".stripMargin
  }

  def dependencies: String =
    """|import sbt._
       |
       |object Version {
       |  final val Scala     = "2.12.0"
       |  final val ScalaTest = "3.0.1"
       |}
       |
       |object Library {
       |  val scalaTest = "org.scalatest" %% "scalatest" % Version.ScalaTest
       |}
       |""".stripMargin

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
      def text(license: License) =
        s"""|## License ##
            |
            |This code is open source software licensed under the ${license.readmeValue} License.""".stripMargin
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
    """|style               = defaultWithAlign
       |danglingParentheses = true
       |indentOperator      = spray
       |
       |spaces {
       |  inImportCurlyBraces = true
       |}
       |""".stripMargin

  def shellPrompt: String =
    """|shellPrompt.in(ThisBuild) := { state =>
       |  val project = Project.extract(state).currentRef.project
       |  s"[$project]> "
       |}
       |""".stripMargin
}
