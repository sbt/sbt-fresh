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

import sbt.IO

private object Template {

  object Licenses {
    sealed trait EnumVal
    case object Apache_v2 extends EnumVal
    case object MIT extends EnumVal
    case object BSD extends EnumVal
    case object BSD3Clause extends EnumVal
    case object GPL_v3 extends EnumVal
    case object None extends EnumVal
  }

  def getLicenseKind(kind: String): Licenses.EnumVal = {
    val licenseKind: Licenses.EnumVal = kind.toLowerCase() match {
      case "apache"     => Licenses.Apache_v2
      case "mit"        => Licenses.MIT
      case "bsd"        => Licenses.BSD
      case "bsd3clause" => Licenses.BSD3Clause
      case "gpl3"       => Licenses.GPL_v3
      case _            => Licenses.None
    }
    licenseKind
  }

  val licensesPaths: Map[Licenses.EnumVal, String] = Map(
    Licenses.Apache_v2 -> "/ApacheLicense",
    Licenses.MIT -> "/MitLicense",
    Licenses.BSD -> "/BSDLicense",
    Licenses.BSD3Clause -> "/BSD3ClauseLicense",
    Licenses.GPL_v3 -> "/GPL3License"
  )

  val headerPluginLicensesSetting: Map[Licenses.EnumVal, String] = Map(
    Licenses.Apache_v2 -> "Apache2_0",
    Licenses.MIT -> "MIT",
    Licenses.BSD -> "BSD2Clause",
    Licenses.BSD3Clause -> "BSD3Clause",
    Licenses.GPL_v3 -> "GPLv3"
  )

  val sbtProjectLicensesMetaData: Map[Licenses.EnumVal, String] = Map(
    Licenses.Apache_v2 -> """("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))""",
    Licenses.MIT -> """("MIT", url("https://opensource.org/licenses/MIT"))""",
    Licenses.BSD -> """("BSD-2-Clause", url("https://opensource.org/licenses/BSD-2-Clause"))""",
    Licenses.BSD3Clause -> """("BSD-3-Clause", url("https://opensource.org/licenses/BSD-3-Clause"))""",
    Licenses.GPL_v3 -> """("GPLv3", url("http://www.gnu.org/licenses/gpl-3.0.en.html"))"""
  )

  def buildProperties: String =
    """|sbt.version = 0.13.11
       |""".stripMargin

  def buildSbt(organization: String, name: String, packageSegments: Vector[String]): String = {
    val `package` = packageSegments.mkString(".")
    val n = if (name.segments.mkString == name) name else s"`$name`"
    s"""|lazy val $n = project
        |  .in(file("."))
        |  .enablePlugins(AutomateHeaderPlugin, GitVersioning)
        |
        |libraryDependencies ++= Vector(
        |  Library.scalaTest % "test"
        |)
        |
        |initialCommands := $TQ|import ${`package`}._
        |                      |$TQ.stripMargin
        |""".stripMargin
  }

  def buildScala(organization: String, author: String, licenseKind: String): String = {
    val licenseMetaData: Option[String] = sbtProjectLicensesMetaData.get(getLicenseKind(licenseKind))
    val headerPluginLicense: Option[String] = headerPluginLicensesSetting.get(getLicenseKind(licenseKind))
    def getLicenseMetaData =
      licenseMetaData match {
        case Some(l) => s"""\n    licenses += $l,"""
        case None    => ""
      }
    def getHeaderPluginLicense =
      headerPluginLicense match {
        case Some(h) => s"""HeaderPlugin.autoImport.headers := Map("scala" -> $h("2016", "$author"))"""
        case None => s"""HeaderPlugin.autoImport.headers := Map("scala" -> (HeaderPattern.cStyleBlockComment,
        \"\"\"|/*
        |           | * Copyright 2016 $author
        |           | */
        |           |\"\"\".stripMargin))"""
      }

    s"""|import com.typesafe.sbt.{ GitPlugin, SbtScalariform }
        |import de.heikoseeberger.sbtheader.HeaderPlugin
        |import de.heikoseeberger.sbtheader.HeaderPattern
        |import de.heikoseeberger.sbtheader.license._
        |import sbt._
        |import sbt.plugins.JvmPlugin
        |import sbt.Keys._
        |import scalariform.formatter.preferences.{ AlignSingleLineCaseStatements, DoubleIndentClassDeclaration }
        |
        |object Build extends AutoPlugin {
        |
        |  override def requires = JvmPlugin && HeaderPlugin && GitPlugin && SbtScalariform
        |
        |  override def trigger = allRequirements
        |
        |  override def projectSettings = Vector(
        |    // Core settings
        |    organization := "$organization", ${getLicenseMetaData}
        |    scalaVersion := Version.Scala,
        |    crossScalaVersions := Vector(scalaVersion.value),
        |    scalacOptions ++= Vector(
        |      "-unchecked",
        |      "-deprecation",
        |      "-language:_",
        |      "-target:jvm-1.8",
        |      "-encoding", "UTF-8"
        |    ),
        |    unmanagedSourceDirectories.in(Compile) := Vector(scalaSource.in(Compile).value),
        |    unmanagedSourceDirectories.in(Test) := Vector(scalaSource.in(Test).value),
        |
        |    // Scalariform settings
        |    SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
        |      .setPreference(AlignSingleLineCaseStatements, true)
        |      .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
        |      .setPreference(DoubleIndentClassDeclaration, true),
        |
        |    // Git settings
        |    GitPlugin.autoImport.git.useGitDescribe := true,
        |
        |    // Header settings
        |    ${getHeaderPluginLicense}
        |  )
        |}
        |""".stripMargin
  }

  def dependencies: String =
    """|import sbt._
       |
       |object Version {
       |  final val Scala     = "2.11.8"
       |  final val ScalaTest = "3.0.0-RC2"
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

  def getLicense(kind: Licenses.EnumVal): Option[String] = {
    val path = licensesPaths.get(kind)
    println(s"path $path")
    path match {
      case Some(s) =>
        val stream = getClass.getResourceAsStream(s)
        Some(scala.io.Source.fromInputStream(stream).getLines().mkString("\n"))
      case None => None
    }
  }

  def license(kind: String): Option[String] = {
    val licenseKind: Licenses.EnumVal = getLicenseKind(kind)
    getLicense(licenseKind)
  }

  def notice(author: String): String =
    s"""|Copyright 2016 $author
        |""".stripMargin

  def `package`(packageSegments: Vector[String], author: String): String = {
    val superPackage = packageSegments.init.mkString(".")
    val lastSegment = packageSegments.last
    s"""|
        |package $superPackage
        |
        |package object $lastSegment {
        |
        |  type Traversable[+A] = scala.collection.immutable.Traversable[A]
        |  type Iterable[+A] = scala.collection.immutable.Iterable[A]
        |  type Seq[+A] = scala.collection.immutable.Seq[A]
        |  type IndexedSeq[+A] = scala.collection.immutable.IndexedSeq[A]
        |}
        |""".stripMargin
  }

  def plugins: String =
    """|addSbtPlugin("com.typesafe.sbt"  % "sbt-git"         % "0.8.5")
       |addSbtPlugin("org.scalariform"   % "sbt-scalariform" % "1.6.0")
       |//addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "1.5.1")
       |
       |// is a SNAPSHOT version of sbt-header plugin (commit hash: 7045c0afc1724106b83766d3ae3ce31a5ef18acd).
       |// It used here because v1.5.1 doesn't has yet chooses of licenses kind.
       |// TODO: should be replaced witn v.1.5.2 (or newest version) then it be ready.
       |addSbtPlugin("de.heikoseeberger" % "sbt-header" % "1.5.1-SNP")
       |
       |// unofficial bintray repo for sbt-header:1.5.1-SNP only
       |resolvers += Resolver.url(
       |  "bintray-strobe-sbt-plugins",
       |  url("http://dl.bintray.com/strobe/sbt-plugins"))(
       |  Resolver.ivyStylePatterns)
       |""".stripMargin

  def readme(name: String): String =
    s"""|# $name #
        |
        |Welcome to $name!
        |
        |## Contribution policy ##
        |
        |Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.
        |
        |## License ##
        |
        |This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
        |""".stripMargin

  def shellPrompt: String =
    """|shellPrompt.in(ThisBuild) := (state => s"[${Project.extract(state).currentRef.project}]> ")
       |""".stripMargin
}
