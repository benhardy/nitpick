package net.bhardy.nitpick.service

import net.bhardy.nitpick.util.{EnvironmentProperties, EnvironmentConfig}
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.Matchers.anyObject

import java.io.IOException
import org.eclipse.jgit.api.errors.{GitAPIException,InvalidRemoteException}
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import java.io.File

class ReviewServiceImplSpec extends FunSpec with MustMatchers with MockitoSugar {

  val envProps = EnvironmentProperties.from((key:String) => key match {
    case "review.checkout.directory" => Some("/tmp/checkouts")
    case _ => throw new IllegalArgumentException
  })
  implicit val conf = new EnvironmentConfig(envProps)
  val cmd = CreateReviewCommand("file:///tmp/a/repo","master")
  val service = new ReviewServiceImpl {
    override def getNextReviewId:Int = 42
  }
  val mockCloner = mock[(String,File)=>Unit]

  describe("createReview") {
    it("should return a Review with a repo cloning") {
      val review = service.createReview(cmd, mockCloner)
      review.reviewId must be === 42
      verify(mockCloner).apply("file:///tmp/a/repo", new File("/tmp/checkouts/review42"))
    }
    it("should encapsulate IO exceptions with a CreateReviewException") {
      intercept[CreateReviewException] {
        val ioFailCloner: (String,File)=>Unit = { (a,b) =>
          throw new IOException()
        }
        val review = service.createReview(cmd, ioFailCloner)
      }
    }
    it("should encapsulate Git exceptions with a CreateReviewException") {
      intercept[CreateReviewException] {
        val gitFailCloner: (String,File)=>Unit = { (a,b) =>
          throw new InvalidRemoteException("wat")
        }
        val review = service.createReview(cmd, gitFailCloner)
      }
    }
      
  }
}


