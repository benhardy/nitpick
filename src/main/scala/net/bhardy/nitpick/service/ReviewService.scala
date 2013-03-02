package net.bhardy.nitpick.service

import net.bhardy.nitpick.Review
import java.io.File
import org.eclipse.jgit.api.CreateBranchCommand
import net.bhardy.nitpick.util.EnvironmentConfig

/**
 * Affected Paths are used in the review view to show the tree of affected
 * paths in any particular set of revisions.
 */
sealed trait AffectedPath {
  def name: String
}

/**
 * Leaf node in an affected path tree is just a file.
 */
case class AffectedFile(name: String)
  extends AffectedPath {
}

/**
 * Branch node in an affected path tree is a directory. Which of course
 * could contain other files or directories. Or nothing (empty List).
 */
case class AffectedDirectory(name: String, children: List[AffectedPath])
  extends AffectedPath {
}

object AffectedDirectory {
  def apply(name: String, singlePath: AffectedPath): AffectedDirectory = {
    AffectedDirectory(name, singlePath :: Nil)
  }
}

/**
 * handy stuff for building an AffectedPath tree
 */
trait AffectedPathBuilding {
  def files(names:String*) = names.map { AffectedFile(_) }.toList

  implicit def file(name: String) = AffectedFile(name)

  implicit def kidList(kids: AffectedPath*) = kids.toList

  class DirBuilder(name:String) {
    def apply(kids: =>List[AffectedPath]) = AffectedDirectory(name, kids)
    def apply(kids: AffectedPath) = AffectedDirectory(name, kids :: Nil)
    def apply(kids: AffectedPath*) = AffectedDirectory(name, kids.toList)
  }
  implicit def dir(name: String) = new DirBuilder(name)
}

case class CreateReviewCommand(
                                gitRepoSpec:String, // TODO figure out which jgit type this is
                                gitBranch:String)

/**
 * Service entry point for data relating to reviews.
 */
trait ReviewService {
  /**
   * Create a tree of affected paths for a review
   * @param forReview - the review for changes affecting those paths
   * @return
   */
  def affectedFiles(forReview: Review): AffectedDirectory

  def createReview(creation: CreateReviewCommand): Review
}

/**
 *
 */
class ReviewServiceImpl(implicit envConfig:EnvironmentConfig) extends ReviewService with AffectedPathBuilding {

  import org.eclipse.jgit.api.Git

  val checkoutParentDir = envConfig.reviewCheckoutDirectory

  def affectedFiles(forReview: Review): AffectedDirectory = {
    dir(".") {
      dir("src") {
        dir("main")(
          dir("scala"){
            files("jaded.scala", "pom.xml")
          },
          file("dork.txt"),
          dir("things") {
            file("mutton.txt")
          }
        )
      }
    }
  }

  def createReview(creation:CreateReviewCommand): Review = {
    import collection.JavaConversions._
    val newReviewId = 2
    val checkoutDir = new File(checkoutParentDir + "/review" + newReviewId)

    val repo = Git.cloneRepository().
      setURI(creation.gitRepoSpec).
      setCloneAllBranches(true).
      setDirectory(checkoutDir).
      call()
    val ref = Git.open(checkoutDir).checkout().
      setCreateBranch(true).
      setName(creation.gitBranch).
      setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).
      setStartPoint("origin/" + creation.gitBranch).
      setForce(true).
      call()
    Review(newReviewId) // TODO return something real
  }
}