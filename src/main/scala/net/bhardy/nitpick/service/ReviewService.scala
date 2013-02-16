package net.bhardy.nitpick.service

import net.bhardy.nitpick.Review


sealed trait AffectedPath {
  def name: String
}

case class AffectedFile(name: String) extends AffectedPath

case class AffectedDirectory(name: String, children: List[AffectedPath]) extends AffectedPath {
}

object AffectedDirectory {
  def apply(name: String, singlePath: AffectedPath): AffectedDirectory = {
    AffectedDirectory(name, singlePath :: Nil)
  }
}

/** shortcuts */
object AffectedPath {
  def file(name:String) = AffectedFile(name)
  def dir(name:String) = AffectedDirectory(name, Nil)
  def dir(name:String, child:AffectedPath) = AffectedDirectory(name, child)
  def dir(name:String, children:List[AffectedPath]) = AffectedDirectory(name, children)
}

trait ReviewService {
  def affectedFiles(forReview: Review): AffectedDirectory
}

class ReviewServiceImpl extends ReviewService {
  def affectedFiles(forReview: Review): AffectedDirectory = {
    import AffectedPath._
    dir("/",
      dir("src",
        dir("main",
          dir("scala",
            file("jaded.sala") ::
            file("pom.xml") ::
            Nil
          )
        )
      )
    )
  }
}