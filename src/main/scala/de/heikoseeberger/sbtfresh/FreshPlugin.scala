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

import sbt.complete.DefaultParsers
import sbt.plugins.JvmPlugin
import sbt.{ AutoPlugin, Command, Keys, Project, State, ThisBuild, settingKey }
import scala.reflect.runtime.universe.{ TypeTag, typeTag }

object FreshPlugin extends AutoPlugin {
  import Fresh._

  object autoImport {

    val freshAuthor = settingKey[String](s"""Author – value of "user.name" sys prop or "$FreshAuthor" by default""")

    val freshName = settingKey[String](s"""Build name – name of build directory by default""")

    val freshOrganization = settingKey[String](s"""Build organization – "$FreshOrganization" by default""")
  }

  private final val FreshOrganization = "default"

  private final val FreshAuthor = "default"

  override def requires = JvmPlugin

  override def trigger = allRequirements

  override def projectSettings = super.projectSettings ++ Vector(
    Keys.commands += freshCommand,
    autoImport.freshOrganization := FreshOrganization,
    autoImport.freshName := Keys.baseDirectory.value.getName,
    autoImport.freshAuthor := sys.props.get("user.name").getOrElse(FreshAuthor)
  )

  private def freshCommand = Command("fresh")(parser)(effect)

  private def parser(state: State) = {
    def arg[A <: Arg: TypeTag](ctor: String => A) = { // ClassTag and getSimpleName broken for doubly nested classes: https://issues.scala-lang.org/browse/SI-2034
      import DefaultParsers._
      val name = typeTag[A].tpe.typeSymbol.name.toString
      (Space ~> name.decapitalize ~> "=" ~> StringBasic).map(ctor)
    }
    (arg(Arg.Organization) | arg(Arg.Name) | arg(Arg.Author)).*.map(_.toVector)
  }

  private def effect(state: State, args: Vector[Arg]) = {
    val baseDir = Project.extract(state).get(Keys.baseDirectory.in(ThisBuild))
    val organization = Project.extract(state).get(autoImport.freshOrganization)
    val name = Project.extract(state).get(autoImport.freshName)
    val author = Project.extract(state).get(autoImport.freshAuthor)
    val fresh = new Fresh(baseDir.toPath, organization, name, author, args)

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

    state.reboot(true)
  }
}
