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
import sbt.complete.{ DefaultParsers, Parser }
import sbt.plugins.JvmPlugin
import sbt.{
  AutoPlugin,
  Command,
  Keys,
  Project,
  SettingKey,
  State,
  ThisBuild,
  settingKey
}
import scala.collection.breakOut

object FreshPlugin extends AutoPlugin {

  final object autoImport {

    val freshOrganization: SettingKey[String] =
      settingKey(s"""Build organization – "$DefaultOrganization" by default""")

    val freshName: SettingKey[String] =
      settingKey(s"""Build name – name of build directory by default""")

    val freshAuthor: SettingKey[String] =
      settingKey(
        s"""Author – value of "user.name" system property or "$DefaultAuthor" by default"""
      )

    val freshLicense: SettingKey[Option[License]] =
      settingKey(
        s"""Optional license (one of $License) – `$DefaultLicense` by default"""
      )

    val freshSetUpGit: SettingKey[Boolean] =
      settingKey(
        "Initialize a Git repo and create an initial commit – `true` by default"
      )
  }

  private final object Arg {
    final val Organization = "organization"
    final val Name         = "name"
    final val Author       = "author"
    final val License      = "license"
    final val SetUpGit     = "setUpGit"
  }

  private final case class Args(organization: Option[String],
                                name: Option[String],
                                author: Option[String],
                                license: Option[License],
                                setUpGit: Option[Boolean])

  private final val DefaultOrganization = "default"
  private final val DefaultAuthor       = "default"
  private final val DefaultLicense      = Some(License.Apache2_0)

  override def requires = JvmPlugin

  override def trigger = allRequirements

  override def projectSettings = {
    import autoImport._

    super.projectSettings ++
    Vector(
      Keys.commands += Command("fresh")(parser)(effect),
      freshOrganization := DefaultOrganization,
      freshName := Keys.baseDirectory.value.getName,
      freshAuthor := sys.props.getOrElse("user.name", DefaultAuthor),
      freshLicense := DefaultLicense,
      freshSetUpGit := true
    )
  }

  private def parser(state: State) = {
    import DefaultParsers._
    def arg[A](name: String, parser: Parser[A]) =
      Space ~> name.decapitalize ~> "=" ~> parser
    val licenseParser = {
      val nameToLicense: Map[String, License] =
        License.values.map(l => l.toString -> l)(breakOut)
      val head +: tail = License.values.toVector.map(_.toString).sorted
      tail.foldLeft(head: Parser[String])(_ | _).map(nameToLicense)
    }
    val args =
      arg(Arg.Organization, NotQuoted).? ~
      arg(Arg.Name, NotQuoted).? ~
      arg(Arg.Author, token(StringBasic)).? ~ // Without token tab completion becomes non-computable!
      arg(Arg.License, licenseParser).? ~
      arg(Arg.SetUpGit, Bool).?
    args.map { case o ~ n ~ a ~ l ~ g => Args(o, n, a, l, g) }
  }

  private def effect(state: State, args: Args) = {
    import autoImport._

    def setting[A](key: SettingKey[A]) = Project.extract(state).get(key)

    val buildDir     = setting(Keys.baseDirectory.in(ThisBuild)).toPath
    val organization = args.organization.getOrElse(setting(freshOrganization))
    val name         = args.name.getOrElse(setting(freshName))
    val author       = args.author.getOrElse(setting(freshAuthor))
    val license      = args.license.orElse(setting(freshLicense))
    val setUpGit     = args.setUpGit.getOrElse(setting(freshSetUpGit))

    val fresh = new Fresh(buildDir, organization, name, author, license)
    fresh.writeBuildProperties()
    fresh.writeBuildSbt()
    fresh.writeBuildScala()
    fresh.writeDependencies()
    fresh.writeGitignore()
    fresh.writeLicense()
    fresh.writeNotice()
    fresh.writePackage()
    fresh.writePlugins()
    fresh.writeReadme()
    fresh.writeScalafmt()
    fresh.writeShellPrompt()
    if (setUpGit) fresh.initialCommit()

    state.reboot(true)
  }
}
