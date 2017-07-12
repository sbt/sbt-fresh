addSbtPlugin("com.dwijnand"      % "sbt-travisci"  % "1.1.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"       % "0.9.3")
addSbtPlugin("com.lucidchart"    % "sbt-scalafmt"  % "1.8")
addSbtPlugin("de.heikoseeberger" % "sbt-header"    % "2.0.0")
addSbtPlugin("me.lessis"         % "bintray-sbt"   % "0.3.0")

libraryDependencies ++= Seq(
  "org.scala-sbt" % "scripted-plugin" % sbtVersion.value,
  "org.slf4j"     % "slf4j-nop"       % "1.7.25" // Needed by sbt-git
)
