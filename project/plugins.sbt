addSbtPlugin("com.geirsson"      % "sbt-scalafmt"    % "0.5.3")
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"         % "0.8.5")
addSbtPlugin("de.heikoseeberger" % "sbt-header"      % "1.6.0")
addSbtPlugin("me.lessis"         % "bintray-sbt"     % "0.3.0")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
