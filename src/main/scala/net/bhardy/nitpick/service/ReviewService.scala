package net.bhardy.nitpick.service

import net.bhardy.nitpick.Review
import java.io._
import org.eclipse.jgit.api.errors.GitAPIException
import net.bhardy.nitpick.util.{CounterFile, EnvironmentConfig}
import net.bhardy.nitpick.{ReviewId,Review}
import java.util.Properties

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
                                gitBranch:String,
                                diffBase:String)

class CreateReviewException(msg:String) extends RuntimeException(msg)

/**
 * Service entry point for data relating to reviews.
 */
trait ReviewService {
  /**
   * Create a tree of affected paths for a review
   * @param forReview - the review for changes affecting those paths
   * @return
   */
  def affectedFiles(forReview: ReviewId): AffectedDirectory

  def createReview(creation: CreateReviewCommand): Review

  def fetch(reviewId: ReviewId): Review
}

/**
 *
 */
class ReviewServiceImpl(implicit envConfig:EnvironmentConfig) extends ReviewService with AffectedPathBuilding {

  import org.slf4j.{Logger, LoggerFactory}
  import org.eclipse.jgit.storage.file.FileRepository
  import org.eclipse.jgit.revwalk.{RevCommit,RevWalk}
  import org.eclipse.jgit.diff.{DiffFormatter,RawTextComparator,DiffEntry}
  import org.eclipse.jgit.util.io.DisabledOutputStream
  import org.eclipse.jgit.lib.{ObjectId,Ref,Repository}

  val logger =  LoggerFactory.getLogger(getClass)


  val checkoutParentDir = envConfig.reviewCheckoutDirectory

  def gitObjectId(repo:Repository, gitString:String): Option[ObjectId] = {
    val tryRef: Option[Ref] = Option(repo.getRef(gitString))
    val oid1 = tryRef.map { ref =>
        val oid = ref.getObjectId
        logger.info(s"2. found ref $gitString in repo, converted to objectId "+oid)
        oid
      }
    oid1.orElse {
        logger.info(s"3. Could not find ref $gitString in repo")
        val desperation = Option(repo.resolve(gitString))
        if (!desperation.isDefined) {
          logger.info(s"4. Could not find $gitString in repo as ref or an objectId, i am sad.")
        } else {
          logger.info(s"5. Could not find $gitString in repo as ref but found as an objectId, yay")
        }
        desperation
      }
  }

  def affectedFiles(reviewId: ReviewId): AffectedDirectory = {
    val review = fetch(reviewId)
    val repository = new FileRepository(cloneDirectory(reviewId))
    val revWalk = new RevWalk(repository)
    logger.info(s"Hey. gonna try getting affected files for $review")
    val res = for {
      fromObject <- gitObjectId(repository, review.diffBase)
      toObject <- gitObjectId(repository, review.gitBranch)
    } yield {
      val commit: RevCommit = revWalk.parseCommit(toObject)
      val parent: RevCommit = revWalk.parseCommit(fromObject)
      val df: DiffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)
      df.setRepository(repository)
      df.setDiffComparator(RawTextComparator.DEFAULT)
      df.setDetectRenames(true)
      import scala.collection.JavaConversions._
      val diffs:List[DiffEntry] = df.scan(parent.getTree, commit.getTree).toList
      for (diff <- diffs) {
          logger.info(List("FOUND: ",
                diff.getChangeType().name(), 
                diff.getNewMode().getBits(), 
                diff.getNewPath()).mkString(" "))
      }
    }
    logger.info(s"res = $res")

    AffectedDirectory(".", Nil)
  }

  protected def getNextReviewId: ReviewId = {
    val path = new File(checkoutParentDir + "/review-highest")
    val counterFile = new CounterFile(path)
    ReviewId(counterFile.next)
  }

  def reviewDirectory(reviewId:ReviewId): File = {
    new File(checkoutParentDir, "review" + reviewId.id)
  }
  def cloneDirectory(reviewId:ReviewId): File = {
    new File(reviewDirectory(reviewId), "repo")
  }

  def createReview(creation: CreateReviewCommand,
                    cloner: (String,File) => Unit = clone): Review = {

    try {
      val nextReviewId = getNextReviewId
      val reviewDir = reviewDirectory(nextReviewId)
      reviewDir.mkdirs
      val repo = cloner(creation.gitRepoSpec, cloneDirectory(nextReviewId))
      saveReviewInfo(reviewDir, creation)
      Review(nextReviewId, creation.gitBranch, creation.diffBase)
    }
    catch {
      case e:IOException => throw new CreateReviewException(e.getMessage)
      case e:GitAPIException => throw new CreateReviewException(e.getMessage)
    }
  }

  def saveReviewInfo(reviewDir:File, creation:CreateReviewCommand) {
    val reviewPropsPath = new File(reviewDir, "review.properties")
    val reviewProps = new Properties
    reviewProps.setProperty("git.repo.spec", creation.gitRepoSpec)
    reviewProps.setProperty("git.branch", creation.gitBranch)
    reviewProps.setProperty("git.diff.base", creation.diffBase)
    val c:OutputStream = new FileOutputStream(reviewPropsPath)
    using(c) { os =>
      reviewProps.store(os, "")
    }
  }

  def fetch(reviewId:ReviewId): Review = {
    val reviewDir = reviewDirectory(reviewId)
    val reviewPropsPath = new File(reviewDir, "review.properties")
    val reviewProps = new Properties
    val c:InputStream =  new FileInputStream(reviewPropsPath)
    using(c) { is =>
      reviewProps.load(is)
    }
    val branch = Option(reviewProps.getProperty("git.branch")).getOrElse("")
    val diffBase = Option(reviewProps.getProperty("git.diff.base")).getOrElse("")
    Review(reviewId, branch, diffBase)
  }

  def using[C <: Closeable](c:C)(block:C=>Unit) {
    try {
      block(c)
    } finally {
      try { c.close }
      catch { case e:Exception => }
    }
  }

  // todo write an integration test that exercises this
  def clone(gitRepoSpec: String, cloneDir:File) : Unit = {
    import org.eclipse.jgit.api.Git
    Git.cloneRepository().
    setURI(gitRepoSpec).
    setCloneAllBranches(true).
    setDirectory(cloneDir).
    call()
  }

  def createReview(creation: CreateReviewCommand) = createReview(creation, clone)
}
