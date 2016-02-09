import de.heikoseeberger.sbtfresh.FreshPlugin
import sbt.AutoPlugin
import sbt.plugins.JvmPlugin

object Build extends AutoPlugin {

  override def requires = JvmPlugin && FreshPlugin

  override def trigger = allRequirements
}
