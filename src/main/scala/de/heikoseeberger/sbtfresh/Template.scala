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

import java.util.Calendar

private object Template {

  sealed trait License
  object License {
    case object ApacheV2   extends License
    case object MIT        extends License
    case object BSD        extends License
    case object BSD3Clause extends License
    case object GPLV3      extends License
    case object None       extends License
  }

  private val year = Calendar.getInstance().get(Calendar.YEAR)

  private val licensesPaths =
    Map(
      License.ApacheV2   -> "/ApacheLicense",
      License.MIT        -> "/MitLicense",
      License.BSD        -> "/BSDLicense",
      License.BSD3Clause -> "/BSD3ClauseLicense",
      License.GPLV3      -> "/GPL3License"
    )

  private val headerPluginLicenseSetting =
    Map(
      License.ApacheV2   -> "Apache2_0",
      License.MIT        -> "MIT",
      License.BSD        -> "BSD2Clause",
      License.BSD3Clause -> "BSD3Clause",
      License.GPLV3      -> "GPLv3"
    )

  private val buildScalaLicenseMetaData =
    Map(
      License.ApacheV2   -> """("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))""",
      License.MIT        -> """("MIT", url("https://opensource.org/licenses/MIT"))""",
      License.BSD        -> """("BSD-2-Clause", url("https://opensource.org/licenses/BSD-2-Clause"))""",
      License.BSD3Clause -> """("BSD-3-Clause", url("https://opensource.org/licenses/BSD-3-Clause"))""",
      License.GPLV3      -> """("GPLv3", url("http://www.gnu.org/licenses/gpl-3.0.en.html"))"""
    )

  private val readmeLicenseMetaData =
    Map(
      License.ApacheV2   -> """[Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0)""",
      License.MIT        -> """[MIT License"](https://opensource.org/licenses/MIT)""",
      License.BSD        -> """[BSD 2 Clause License](https://opensource.org/licenses/BSD-2-Clause)""",
      License.BSD3Clause -> """[BSD 3 Clause License](https://opensource.org/licenses/BSD-3-Clause)""",
      License.GPLV3      -> """[GPLv3 License](http://www.gnu.org/licenses/gpl-3.0.en.html)"""
    )

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
                 license: String): String = {
    val kind                = getLicenseKind(license)
    val licenseMetaData     = buildScalaLicenseMetaData.get(kind)
    val headerPluginLicense = headerPluginLicenseSetting.get(kind)

    def createLicenseMetaData =
      licenseMetaData match {
        case Some(l) =>
          s"""|
              |      licenses += $l,
              |      mappings.in(Compile, packageBin)
              |        += baseDirectory.in(ThisBuild).value / "LICENSE" -> "LICENSE",""".stripMargin
        case None =>
          ""
      }

    def createHeaderPluginLicense =
      headerPluginLicense match {
        case Some(h) =>
          s"""headers := Map("scala" -> $h("$year", "$author"))"""
        case None =>
          s"""headers := Map("scala" -> (HeaderPattern.cStyleBlockComment,
        \"\"\"|/*
        |           | * Copyright $year $author
        |           | */
        |           |\"\"\".stripMargin))"""
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
        |      organization := "$organization",$createLicenseMetaData
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
        |      $createHeaderPluginLicense
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

  def license(kind: String): String = {
    val licenseKind: License = getLicenseKind(kind)

    licensesPaths.map {
      case (k, v) if k == licenseKind =>
        val stream = getClass.getResourceAsStream(v)
        scala.io.Source.fromInputStream(stream).getLines().mkString("\n")
      case _ => ""
    }.reduceLeft(_ + _)
  }

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

  def readme(name: String, license: String): String = {
    val licenseMetaData = readmeLicenseMetaData.get(getLicenseKind(license))

    val licenseText = licenseMetaData match {
      case Some(text) =>
        s"""|## License ##
            |
            |This code is open source software licensed under the $text.""".stripMargin
      case None => ""
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

  private def getLicenseKind(kind: String) =
    kind.toLowerCase() match {
      case "apache"     => License.ApacheV2
      case "mit"        => License.MIT
      case "bsd"        => License.BSD
      case "bsd3clause" => License.BSD3Clause
      case "gpl3"       => License.GPLV3
      case _            => License.None
    }
}
