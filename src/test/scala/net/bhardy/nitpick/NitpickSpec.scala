package net.bhardy.nitpick


import org.scalatest.matchers.{ShouldMatchers, MustMatchers}
import org.scalatest.{FunSuite, FunSpec}
import org.scalatra.test.scalatest._
import service._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers
import service.CreateReviewCommand

class NitpickSpec extends ScalatraSuite with FunSuite with MockitoSugar {

  implicit val rs = mock[ReviewService]
  val error = new CreateReviewException("something went wrong")
  val invalidCreation = CreateReviewCommand("invalid", "master", "origin/master")
  val validCreationDefault = CreateReviewCommand("someuri", "master", "origin/master")
  val validCreationNotDefault = CreateReviewCommand("someuri", "master", "myotherbranch")
  doThrow(error).when(rs).createReview(Matchers.eq(invalidCreation))
  val defaultReview = Review(ReviewId(42), "master", "origin/master")
  val nonDefaultReview = Review(ReviewId(42), "master", "myotherbranch")
  when(rs.createReview(Matchers.eq(validCreationDefault))).thenReturn(defaultReview)
  when(rs.createReview(Matchers.eq(validCreationNotDefault))).thenReturn(nonDefaultReview)
  when(rs.affectedFiles(ReviewId(42))).thenReturn(
    AffectedDirectory(".", List(
      AffectedFile("README.txt")
    ))
  )
  when(rs.fetch(ReviewId(42))).thenReturn(nonDefaultReview)
  addServlet(new NitpickServlet, "/*")

  test("main page") {
    get("/") {
      status must be === 200
      body must include ("Nitpick.")
      body must include ("Create Review")
      body must include ("Current Reviews")
    }
  }

  test("review creation success even without diffbase") {
    post("/review/new", Seq("gitrepo"->"someuri", "branch"->"master")) {
      status must be === 200
      body must be === "{\"id\":42}"
    }
  }
  test("review creation success with diffBase") {
    post("/review/new", Seq("gitrepo"->"someuri", "branch"->"master", "diffbase"->"myotherbranch")) {
      status must be === 200
      body must be === "{\"id\":42}"
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
      status must be === 200
      body must include ("Review #42")
    }
  }

  test("affected file list retrieval success") {
    get("/review/42/affected-files") {
      status must be === 200
      val expected = "{\"name\":\".\",\"children\":[{\"name\":\"README.txt\"}]}"
      body must be === expected
    }
  }

}
