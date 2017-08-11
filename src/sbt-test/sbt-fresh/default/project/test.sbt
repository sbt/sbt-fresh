val pluginVersion = sys.props
  .get("plugin.version")
  .getOrElse(sys.error("Sys prop plugin.version must be defined!"))

addSbtPlugin("de.heikoseeberger" % "sbt-fresh" % pluginVersion)
