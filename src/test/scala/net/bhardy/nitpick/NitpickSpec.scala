package net.bhardy.nitpick


import org.scalatest.matchers.{ShouldMatchers, MustMatchers}
import org.scalatest.{FunSuite, FunSpec}
import org.scalatra.test.scalatest._
import service._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers
import net.bhardy.nitpick.Review
import service.CreateReviewCommand

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
  when(rs.affectedFiles(Review(42))).thenReturn(
    AffectedDirectory(".", List(
      AffectedFile("README.txt")
    ))
  )
  addServlet(new NitpickServlet, "/*")

  test("main page") {
    get("/") {
      status must be === (200)
      body must include ("Nitpick.")
      body must include ("Create Review")
      body must include ("Current Reviews")
    }
  }

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

  test("review retrieval success") {
    get("/review/42") {
      status must be === (200)
      body must include ("Review #42")
    }
  }

  test("affected file list retrieval success") {
    get("/review/42/affected-files") {
      status must be === (200)
      val expected = "{\"name\":\".\",\"children\":[{\"name\":\"README.txt\"}]}"
      body must be === expected
    }
  }

}
