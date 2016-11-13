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

object FreshPlugin extends AutoPlugin {

  final object autoImport {
    val freshAuthor: SettingKey[String] =
      settingKey[String](
        s"""Author – value of "user.name" sys prop or "$FreshAuthor" by default""")

    val freshName: SettingKey[String] =
      settingKey[String](
        s"""Build name – name of build directory by default""")

    val freshOrganization: SettingKey[String] =
      settingKey[String](
        s"""Build organization – "$FreshOrganization" by default""")

    val freshLicense: SettingKey[String] =
      settingKey[String]("License kind")

    val freshSetUpGit: SettingKey[Boolean] =
      settingKey[Boolean](
        "Initialize a Git repo and create an initial commit – true by default")
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
                                license: Option[String],
                                setUpGit: Option[Boolean])

  private final val FreshOrganization = "default"
  private final val FreshAuthor       = "default"
  private final val FreshLicense      = "apache"

  override def requires = JvmPlugin

  override def trigger = allRequirements

  override def projectSettings = {
    import autoImport._
    super.projectSettings ++ Vector(
      Keys.commands += Command("fresh")(parser)(effect),
      freshOrganization := FreshOrganization,
      freshName         := Keys.baseDirectory.value.getName,
      freshAuthor       := sys.props.getOrElse("user.name", FreshAuthor),
      freshLicense      := FreshLicense,
      freshSetUpGit     := true
    )
  }

  private def parser(state: State) = {
    import DefaultParsers._
    def arg[A](name: String, parser: Parser[A]) =
      Space ~> name.decapitalize ~> "=" ~> parser
    val args = arg(Arg.Organization, NotQuoted).? ~
        arg(Arg.Name, NotQuoted).? ~
        arg(Arg.Author, token(StringBasic)).? ~ // Without token tab completion becomes non-computable!
        arg(Arg.License, token(StringBasic)).? ~
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
    val license      = args.license.getOrElse(setting(freshLicense))
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
