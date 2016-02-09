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

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{ Files, Path }

object Fresh {

  sealed trait Arg
  object Arg {
    case class Organization(organization: String) extends Arg
    case class Name(name: String) extends Arg
    case class Author(author: String) extends Arg
  }
}

private class Fresh(
    baseDir: Path,
    setOrganization: String,
    setName: String,
    setAuthor: String,
    args: Seq[Fresh.Arg]
) {
  import Fresh._

  private val organization = args
    .collectFirst { case Arg.Organization(organization) => organization }
    .getOrElse(setOrganization)

  private val name = args
    .collectFirst { case Arg.Name(name) => name }
    .getOrElse(setName)

  private val author = args
    .collectFirst { case Arg.Author(author) => author }
    .getOrElse(setAuthor)

  private val packageSegments = {
    val segments = (organization.segments ++ name.segments).map(_.toLowerCase)
    val (tail, init) = segments
      .tail
      .zip(segments)
      .filter { case (s1, s2) => s1 != s2 }
      .unzip
    init.head +: tail
  }

  def writeBuildProperties(): Path = write("project/build.properties", Template.buildProperties)

  def writeBuildSbt(): Path = write("build.sbt", Template.buildSbt(organization, name, packageSegments))

  def writeBuildScala(): Path = write("project/Build.scala", Template.buildScala(organization, author))

  def writeDependencies(): Path = write("project/Dependencies.scala", Template.dependencies)

  def writeGitignore(): Path = write(".gitignore", Template.gitignore)

  def writeLicense(): Path = write("src/main/resources/LICENSE", Template.license)

  def writeNotice(): Path = write("NOTICE", Template.notice(author))

  def writePackage(): Path = write(
    packageSegments.foldLeft("src/main/scala")(_ + "/" + _) + "/package.scala",
    Template.`package`(packageSegments, author)
  )

  def writePlugins(): Path = write("project/plugins.sbt", Template.plugins)

  def writeReadme(): Path = write("README.md", Template.readme(name))

  private def write(path: String, content: String) = {
    val resolvedPath = baseDir.resolve(path)
    if (resolvedPath.getParent != null) Files.createDirectories(resolvedPath.getParent)
    Files.write(resolvedPath, content.getBytes(UTF_8))
  }
}
