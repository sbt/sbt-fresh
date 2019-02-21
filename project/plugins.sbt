addSbtPlugin("com.dwijnand"      % "sbt-dynver"   % "3.3.0")
addSbtPlugin("com.dwijnand"      % "sbt-travisci" % "1.1.3")
addSbtPlugin("com.geirsson"      % "sbt-scalafmt" % "1.5.1")
addSbtPlugin("de.heikoseeberger" % "sbt-header"   % "5.1.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray"  % "0.5.4")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.26" // Needed by sbt-git
