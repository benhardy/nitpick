package net.bhardy.nitpick

import org.scalatra._
import scalate.ScalateSupport
import service.{CreateReviewCommand, ReviewService}
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


  get("/hello") {
    contentType = "text/html"
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/style.css"/>
      </head>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
        <p/>
        <a href="/review/2">look at review 2</a>
      </body>
    </html>
  }

  post("/review/new") {
    val gitRepo = params("gitrepo")
    val branch = params("branch")
    val creationCommand = CreateReviewCommand(gitRepo, branch)
    val review = reviewService.createReview(creationCommand)
    redirect("/review/" + review.reviewId)
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
