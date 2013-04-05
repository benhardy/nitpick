package net.bhardy.nitpick

import scala.List
/**
 */



case class ReviewId(id: Int)

case class Review(reviewId: ReviewId, 
  gitBranch:String,
  diffBase:String) {

}

