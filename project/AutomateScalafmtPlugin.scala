package org.scalafmt.sbt

import org.scalafmt.bootstrap.ScalafmtBootstrap
import sbt._
import sbt.Keys._

object AutomateScalafmtPlugin extends AutoPlugin {

  object autoImport {
    def automateScalafmtFor(configurations: Configuration*): Seq[Setting[_]] =
      configurations.map { configuration =>
        compile.in(configuration) := {
          automateScalafmt.value
          compile.in(configuration).value
        }
      }
  }

  private val automateScalafmt = taskKey[Unit]("Automate scalafmt")

  override def requires = ScalafmtPlugin

  override def trigger = allRequirements

  override def projectSettings =
    (automateScalafmt := ScalafmtBootstrap.main(List("--non-interactive"))) +:
    autoImport.automateScalafmtFor(Compile, Test)
}
