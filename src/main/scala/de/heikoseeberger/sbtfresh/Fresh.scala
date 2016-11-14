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
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{ Files, Path }
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import org.eclipse.jgit.api.Git

private final class Fresh(buildDir: Path,
                          organization: String,
                          name: String,
                          author: String,
                          license: Option[License]) {

  require(organization.nonEmpty, "organization must not be empty!")
  require(name.nonEmpty, "name must not be empty!")

  private val packageSegments = {
    val all  = (organization.segments ++ name.segments).map(_.toLowerCase)
    val tail = all.tail.zip(all).collect { case (s1, s2) if s1 != s2 => s1 }
    all.head +: tail
  }

  def initialCommit(): Unit = {
    val git = Git.init().setDirectory(buildDir.toFile).call()
    git.add().addFilepattern(".").call()
    git.commit().setMessage("Fresh project, created with sbt-fresh").call()
  }

  def writeBuildProperties(): Path =
    write("project/build.properties", Template.buildProperties)

  def writeBuildSbt(): Path =
    write("build.sbt", Template.buildSbt(organization, name, packageSegments))

  def writeBuildScala(): Path =
    write("project/Build.scala",
          Template.buildScala(organization, author, license))

  def writeDependencies(): Path =
    write("project/Dependencies.scala", Template.dependencies)

  def writeGitignore(): Path =
    write(".gitignore", Template.gitignore)

  def writeLicense(): Unit =
    license.foreach(l => copy("LICENSE", l.id))

  def writeNotice(): Path =
    write("NOTICE", Template.notice(author))

  def writePackage(): Path = {
    val path =
      packageSegments.foldLeft("src/main/scala")(_ + "/" + _) + "/package.scala"
    write(path, Template.`package`(packageSegments, author))
  }

  def writePlugins(): Path =
    write("project/plugins.sbt", Template.plugins)

  def writeReadme(): Path =
    write("README.md", Template.readme(name, license))

  def writeScalafmt(): Path =
    write(".scalafmt.conf", Template.scalafmtConf)

  def writeShellPrompt(): Path =
    write("shell-prompt.sbt", Template.shellPrompt)

  private def write(path: String, content: String) =
    Files.write(resolve(path), content.getBytes(UTF_8))

  private def copy(path: String, name: String) =
    Files.copy(getClass.getResourceAsStream(s"/$name"),
               resolve(path),
               REPLACE_EXISTING)

  private def resolve(path: String) = {
    val resolved = buildDir.resolve(path)
    if (resolved.getParent != null)
      Files.createDirectories(resolved.getParent)
    resolved
  }
}
