package net.bhardy.nitpick

import scala.List
import org.eclipse.jgit.diff.DiffEntry

/**
 */



case class ReviewId(id: Int)

case class Review(reviewId: ReviewId, 
  gitBranch: String,
  diffBase: String,
  diffEntries: Iterable[DiffEntry]
                   ) {

}

