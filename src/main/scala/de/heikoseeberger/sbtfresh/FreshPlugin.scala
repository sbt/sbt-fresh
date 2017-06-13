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
import sbt.{ settingKey, AutoPlugin, Command, Keys, Project, SettingKey, State, ThisBuild }

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
      settingKey(s"""Optional license (one of $licenseIds) – `$DefaultLicense` by default""")

    val freshSetUpGit: SettingKey[Boolean] =
      settingKey("Initialize a Git repo and create an initial commit – `true` by default")

    val freshSetUpTravis: SettingKey[Boolean] =
      settingKey("Configure Travis for Continuous Integration - `true` by default")

    val freshSetUpWartremover: SettingKey[Boolean] =
      settingKey("Include the sbt wartremover plugin - `false` by default")

    val freshUseGitPrompt: SettingKey[Boolean] =
      settingKey(
        "Configure the sbt prompt to use the one provided by sbt-git - `false` by default"
      )

    private def licenseIds = License.values.toVector.sortBy(_.id).mkString(", ")
  }

  private final object Arg {
    final val Organization     = "organization"
    final val Name             = "name"
    final val Author           = "author"
    final val License          = "license"
    final val SetUpGit         = "setUpGit"
    final val SetUpTravis      = "setUpTravis"
    final val SetUpWartremover = "setUpWartremover"
    final val UseGitPrompt     = "useGitPrompt"
  }

  private final case class Args(organization: Option[String],
                                name: Option[String],
                                author: Option[String],
                                license: Option[License],
                                setUpGit: Option[Boolean],
                                setUpTravis: Option[Boolean],
                                setUpWartremover: Option[Boolean],
                                useGitPrompt: Option[Boolean])

  private final val DefaultOrganization = "default"
  private final val DefaultAuthor       = "default"
  private final val DefaultLicense      = Some(License.apache20)

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
      freshSetUpGit := true,
      freshSetUpTravis := true,
      freshSetUpWartremover := false,
      freshUseGitPrompt := false
    )
  }

  private def parser(state: State) = {
    import DefaultParsers._
    def arg[A](name: String, parser: Parser[A]) = Space ~> name.decapitalize ~> "=" ~> parser
    val licenseParser =
      License.values.toVector
        .sortBy(_.id)
        .map(l => (l.id: Parser[String]).map(_ => l))
        .reduceLeft(_ | _)
    val args =
    arg(Arg.Organization, NotQuoted).? ~
    arg(Arg.Name, NotQuoted).? ~
    arg(Arg.Author, token(StringBasic)).? ~ // Without token tab completion becomes non-computable!
    arg(Arg.License, licenseParser).? ~
    arg(Arg.SetUpGit, Bool).? ~
    arg(Arg.SetUpTravis, Bool).? ~
    arg(Arg.SetUpWartremover, Bool).? ~
    arg(Arg.UseGitPrompt, Bool).?
    args.map { case o ~ n ~ a ~ l ~ g ~ t ~ wr ~ gp => Args(o, n, a, l, g, t, wr, gp) }
  }

  private def effect(state: State, args: Args) = {
    import autoImport._

    def setting[A](key: SettingKey[A]) = Project.extract(state).get(key)

    val buildDir         = setting(Keys.baseDirectory.in(ThisBuild)).toPath
    val organization     = args.organization.getOrElse(setting(freshOrganization))
    val name             = args.name.getOrElse(setting(freshName))
    val author           = args.author.getOrElse(setting(freshAuthor))
    val license          = args.license.orElse(setting(freshLicense))
    val setUpGit         = args.setUpGit.getOrElse(setting(freshSetUpGit))
    val setUpTravis      = args.setUpTravis.getOrElse(setting(freshSetUpTravis))
    val setUpWartremover = args.setUpWartremover.getOrElse(setting(freshSetUpWartremover))
    val useGitPrompt     = args.useGitPrompt.getOrElse(setting(freshUseGitPrompt))

    val fresh = new Fresh(buildDir, organization, name, author, license)
    fresh.writeAutomateScalafmtPlugin()
    fresh.writeBuildProperties()
    fresh.writeBuildSbt(useGitPrompt, setUpTravis, setUpWartremover)
    fresh.writeGitignore()
    fresh.writeLicense()
    fresh.writeNotice()
    fresh.writePackage()
    fresh.writePlugins(setUpTravis, setUpWartremover)
    fresh.writeReadme()
    fresh.writeScalafmt()
    if (setUpTravis) fresh.writeTravisYml()
    if (setUpGit) fresh.initialCommit()

    state.reboot(true)
  }
}
