addSbtPlugin("com.dwijnand"      % "sbt-travisci"  % "1.1.1")
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"       % "0.9.3")
addSbtPlugin("com.lucidchart"    % "sbt-scalafmt"  % "1.10")
addSbtPlugin("de.heikoseeberger" % "sbt-header"    % "3.0.1")
addSbtPlugin("org.foundweekends" % "sbt-bintray"   % "0.5.1")

libraryDependencies ++= Seq(
  "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value,
  "org.slf4j"     %  "slf4j-nop"       % "1.7.25" // Needed by sbt-git
)
