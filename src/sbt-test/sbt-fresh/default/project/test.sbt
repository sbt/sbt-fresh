val pluginVersion =
  sys.props.getOrElse("plugin.version", sys.error("Sys prop plugin.version must be defined!"))

addSbtPlugin("de.heikoseeberger" % "sbt-fresh" % pluginVersion)
