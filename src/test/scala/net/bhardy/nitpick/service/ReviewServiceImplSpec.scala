package net.bhardy.nitpick.service

import net.bhardy.nitpick.Review
import net.bhardy.nitpick.ReviewId
import net.bhardy.nitpick.util.{EnvironmentProperties, EnvironmentConfig}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.{GitAPIException,InvalidRemoteException}
import org.mockito.Matchers.anyObject
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar

import java.io.File
import java.io.IOException

class ReviewServiceImplSpec extends FunSpec with MustMatchers with MockitoSugar {

  val envProps = EnvironmentProperties.from((key:String) => key match {
    case "review.checkout.directory" => Some("/tmp/checkouts")
    case _ => throw new IllegalArgumentException
  })
  implicit val conf = new EnvironmentConfig(envProps)
  val cmd = CreateReviewCommand("file:///tmp/a/repo","master", "origin/master")
  val service = new ReviewServiceImpl {
    override def getNextReviewId = ReviewId(42)
  }

  describe("createReview") {
    it("should return a Review upon successful repo cloning") {
      val mockCloner = mock[(String,File)=>Git]
      val review = service.createReview(cmd, mockCloner)
      review.reviewId.id must be === 42
      verify(mockCloner).apply("file:///tmp/a/repo", new File("/tmp/checkouts/review42/repo"))
    }
    it("should encapsulate IO exceptions with a CreateReviewException") {
      intercept[CreateReviewException] {
        val ioFailCloner: (String,File)=>Git = { (a,b) =>
          throw new IOException()
        }
        val review = service.createReview(cmd, ioFailCloner)
      }
    }
    it("should encapsulate Git exceptions with a CreateReviewException") {
      intercept[CreateReviewException] {
        val gitFailCloner: (String,File)=>Git = { (a,b) =>
          throw new InvalidRemoteException("wat")
        }
        val review = service.createReview(cmd, gitFailCloner)
      }
    }
  }

  describe("affectedFiles") {
    it("should read properties file previously created") {
      // TODO
    }
  }
}



