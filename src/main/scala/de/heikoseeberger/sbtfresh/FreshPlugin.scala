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
import scala.reflect.runtime.universe.{ TypeTag, typeTag }

object FreshPlugin extends AutoPlugin {

  object autoImport {
    val freshAuthor = settingKey[String](s"""Author – value of "user.name" sys prop or "$FreshAuthor" by default""")
    val freshName = settingKey[String](s"""Build name – name of build directory by default""")
    val freshOrganization = settingKey[String](s"""Build organization – "$FreshOrganization" by default""")
    val freshSetUpGit = settingKey[Boolean]("Initialize a Git repo and create an initial commit – true by default")
  }

  private sealed trait Arg
  private object Arg {
    case class Organization(value: String) extends Arg
    case class Name(value: String) extends Arg
    case class Author(value: String) extends Arg
    case class SetUpGit(value: Boolean) extends Arg
  }

  private final val FreshOrganization = "default"
  private final val FreshAuthor = "default"

  override def requires = JvmPlugin

  override def trigger = allRequirements

  override def projectSettings = super.projectSettings ++ Vector(
    Keys.commands += freshCommand,
    autoImport.freshOrganization := FreshOrganization,
    autoImport.freshName := Keys.baseDirectory.value.getName,
    autoImport.freshAuthor := sys.props.get("user.name").getOrElse(FreshAuthor),
    autoImport.freshSetUpGit := true
  )

  private def freshCommand = Command("fresh")(parser)(effect)

  private def parser(state: State) = {
    import DefaultParsers._
    def arg[A, B <: Arg: TypeTag](parser: Parser[A])(ctor: A => B) = { // ClassTag and getSimpleName broken for doubly nested classes: https://issues.scala-lang.org/browse/SI-2034
      val name = typeTag[B].tpe.typeSymbol.name.toString
      (Space ~> name.decapitalize ~> "=" ~> parser).map(ctor)
    }
    val args = arg(StringBasic)(Arg.Organization) |
      arg(StringBasic)(Arg.Name) |
      arg(StringBasic)(Arg.Author) |
      arg(Bool)(Arg.SetUpGit)
    args.*.map(_.toVector)
  }

  private def effect(state: State, args: Vector[Arg]) = {
    def setting[A](key: SettingKey[A]) = Project.extract(state).get(key)
    def argOrSetting[A](default: SettingKey[A])(f: Arg ?=> A) = args.collectFirst(f).getOrElse(setting(default))

    val buildDir = setting(Keys.baseDirectory.in(ThisBuild)).toPath
    val organization = argOrSetting(autoImport.freshOrganization) { case Arg.Organization(value) => value }
    val name = argOrSetting(autoImport.freshName) { case Arg.Name(value) => value }
    val author = argOrSetting(autoImport.freshAuthor) { case Arg.Author(value) => value }
    val setUpGit = argOrSetting(autoImport.freshSetUpGit) { case Arg.SetUpGit(value) => value }

    val fresh = new Fresh(buildDir, organization, name, author)
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

    if (setUpGit) fresh.initialCommit()

    state.reboot(true)
  }
}
