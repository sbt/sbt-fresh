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
import sbt.{ AutoPlugin, Command, Keys, Project, SettingKey, State, ThisBuild, settingKey }

object FreshPlugin extends AutoPlugin {

  object autoImport {
    val freshAuthor = settingKey[String](s"""Author – value of "user.name" sys prop or "$FreshAuthor" by default""")
    val freshName = settingKey[String](s"""Build name – name of build directory by default""")
    val freshOrganization = settingKey[String](s"""Build organization – "$FreshOrganization" by default""")
    val freshLicense = settingKey[String]("License kind")
    val freshSetUpGit = settingKey[Boolean]("Initialize a Git repo and create an initial commit – true by default")
  }

  private object Arg {
    final val Organization = "organization"
    final val Name = "name"
    final val Author = "author"
    final val License = "license"
    final val SetUpGit = "setUpGit"
  }

  private case class Args(organization: Option[String], name: Option[String], author: Option[String], license: Option[String], setUpGit: Option[Boolean])

  private final val FreshOrganization = "default"
  private final val FreshAuthor = "default"
  private final val FreshLicense = "apache"

  override def requires = JvmPlugin

  override def trigger = allRequirements

  override def projectSettings = super.projectSettings ++ Vector(
    Keys.commands += freshCommand,
    autoImport.freshOrganization := FreshOrganization,
    autoImport.freshName := Keys.baseDirectory.value.getName,
    autoImport.freshAuthor := sys.props.get("user.name").getOrElse(FreshAuthor),
    autoImport.freshLicense := FreshLicense,
    autoImport.freshSetUpGit := true
  )

  private def freshCommand = Command("fresh")(parser)(effect)

  private def parser(state: State) = {
    import DefaultParsers._
    def arg[A](name: String, parser: Parser[A]) = Space ~> name.decapitalize ~> "=" ~> parser
    val args = arg(Arg.Organization, NotQuoted).? ~
      arg(Arg.Name, NotQuoted).? ~
      arg(Arg.Author, token(StringBasic)).? ~ // Without token tab completion becomes non-computable!
      arg(Arg.License, token(StringBasic)).? ~
      arg(Arg.SetUpGit, Bool).?
    args.map { case o ~ n ~ a ~ l ~ g => Args(o, n, a, l, g) }
  }

  private def effect(state: State, args: Args) = {
    def setting[A](key: SettingKey[A]) = Project.extract(state).get(key)

    val buildDir = setting(Keys.baseDirectory.in(ThisBuild)).toPath
    val organization = args.organization.getOrElse(setting(autoImport.freshOrganization))
    val name = args.name.getOrElse(setting(autoImport.freshName))
    val author = args.author.getOrElse(setting(autoImport.freshAuthor))
    val license = args.license.getOrElse(setting(autoImport.freshLicense))
    val setUpGit = args.setUpGit.getOrElse(setting(autoImport.freshSetUpGit))

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
