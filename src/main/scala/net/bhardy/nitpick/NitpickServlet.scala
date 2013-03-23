package net.bhardy.nitpick

import org.scalatra._
import scalate.ScalateSupport
import service.{CreateReviewException, CreateReviewCommand, ReviewService}
// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

class NitpickServlet(implicit reviewService:ReviewService)
  extends ScalatraServlet
  with ScalateSupport  with JacksonJsonSupport {

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  get("/") {
    contentType = "text/html"
    ssp("/layouts/main.ssp", "title" -> "Nitpick.")
  }

  post("/review/new") {
    try {
      val gitRepo = params("gitrepo")
      val branch = params("branch")
      val against = params("against")
      val creationCommand = CreateReviewCommand(gitRepo, branch, against)
      val review = reviewService.createReview(creationCommand)
      review
    }
    catch {
      case e: CreateReviewException => ActionResult(
        status = ResponseStatus(403, "Forbidden"),
        body = "Sorry, couldn't create that review: " + e.getMessage,
        headers = Map()
      )
    }
  }

  get("/review/:reviewId") {
    val reviewId: Int = params("reviewId").toInt
    val review = Review(reviewId)

    contentType = "text/html"
    ssp("/layouts/review.ssp", "review" -> review, "title" -> "some review")
  }

  get("/review/:reviewId/affected-files") {
    val reviewId: Int = params("reviewId").toInt
    val review = Review(reviewId)
    reviewService.affectedFiles(review)
  }

  notFound {
    // remove content type in case it was set through an action
    contentType = null
    // Try to render a ScalateTemplate if no route matched
    findTemplate(requestPath) map { path =>
      contentType = "text/html"
      layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}
