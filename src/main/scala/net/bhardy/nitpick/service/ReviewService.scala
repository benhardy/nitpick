package net.bhardy.nitpick.service

import net.bhardy.nitpick.Review
import collection.immutable


sealed trait AffectedPath {
  def name: String
}

case class AffectedFile(name: String)
  extends AffectedPath {
}

case class AffectedDirectory(name: String, children: List[AffectedPath])
  extends AffectedPath {
}

object AffectedDirectory {
  def apply(name: String, singlePath: AffectedPath): AffectedDirectory = {
    AffectedDirectory(name, singlePath :: Nil)
  }
}

/** shortcuts */
trait AffectedPathBuilding {
  def files(names:String*) = names.map { AffectedFile(_) }.toList

  implicit def file(name: String) = AffectedFile(name)

  implicit def kidList(kids: AffectedPath*) = kids.toList

  class DirBuilder(name:String) {
    def apply(kids: =>List[AffectedPath]) = AffectedDirectory(name, kids)
    def apply(kids: AffectedPath) = AffectedDirectory(name, kids :: Nil)
  }
  implicit def dir(name: String) = new DirBuilder(name)
}

trait ReviewService {
  def affectedFiles(forReview: Review): AffectedDirectory
}

class ReviewServiceImpl extends ReviewService with AffectedPathBuilding {
  def affectedFiles(forReview: Review): AffectedDirectory = {
    dir(".") {
      dir("src") {
        dir("main") {
          dir("scala") {
            files("jaded.scala", "pom.xml")
          }
        }
      }
    }
  }
}