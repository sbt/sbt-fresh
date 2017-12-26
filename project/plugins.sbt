addSbtPlugin("com.dwijnand"      % "sbt-travisci"  % "1.1.1")
addSbtPlugin("com.lucidchart"    % "sbt-scalafmt"  % "1.15")
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"       % "0.9.3")
addSbtPlugin("de.heikoseeberger" % "sbt-header"    % "4.0.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray"   % "0.5.2")

libraryDependencies ++= Seq(
  "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value,
  "org.slf4j"     %  "slf4j-nop"       % "1.7.25" // Needed by sbt-git
)
