package net.bhardy.nitpick

import org.scalatra.test.specs2._
import org.scalatest.mock.MockitoSugar
import service.ReviewService

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class NitpickSpec extends ScalatraSpec with MockitoSugar { def is =
  "GET / on Nitpick"                     ^
    "should return status 200"                  ! root200^
                                                end

  implicit val reviewServiceDependency = mock[ReviewService]
  addServlet(new NitpickServlet, "/*")

  def root200 = get("/")  {
    status must_== 200
  }
}
