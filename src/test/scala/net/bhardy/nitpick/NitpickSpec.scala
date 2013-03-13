package net.bhardy.nitpick


import org.scalatest.matchers.{ShouldMatchers, MustMatchers}
import org.scalatest.{FunSuite, FunSpec}
import org.scalatra.test.scalatest._
import service.{CreateReviewException, CreateReviewCommand, ReviewService}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers

class NitpickSpec extends ScalatraSuite with FunSuite with MockitoSugar {
  /*
    def is =
    "GET / on Nitpick"                     ^
      "should return status 200"                  ! root200^
                                                  end
  */
  implicit val rs = mock[ReviewService]
  val error = new CreateReviewException("something went wrong")
  val invalidCreation = CreateReviewCommand("invalid", "master")
  val validCreation = CreateReviewCommand("someuri", "master")
  doThrow(error).when(rs).createReview(Matchers.eq(invalidCreation))
  when(rs.createReview(Matchers.eq(validCreation))).thenReturn(Review(42))
  addServlet(new NitpickServlet, "/*")

  test("review creation success") {
    post("/review/new", Seq("gitrepo"->"someuri", "branch"->"master")) {
      status must be === (200)
      body must be === "{\"reviewId\":42}"
    }
  }

  test("review creation failure") {
    post("/review/new", Seq("gitrepo"->"invalid", "branch"->"master")) {
      status must be === (403)
      body must be === ("Sorry, couldn't create that review: something went wrong")
    }
  }

}
