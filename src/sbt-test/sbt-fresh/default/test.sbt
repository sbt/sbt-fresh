import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import scala.collection.JavaConverters._

val checkInitialCommit = taskKey[Unit]("Verify the initial commit")
checkInitialCommit := {
  val git = new Git((new FileRepositoryBuilder).setWorkTree(baseDirectory.value).build())
  val commits = git.log().call().asScala.toVector
  assert(commits.size == 1, "There must be exactly one commit!")
}
