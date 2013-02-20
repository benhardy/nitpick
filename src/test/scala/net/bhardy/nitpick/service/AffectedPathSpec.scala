package net.bhardy.nitpick.service

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers

/**
 */
class AffectedPathSpec extends FunSpec with MustMatchers {

  class Client extends AffectedPathBuilding {
    def dirs = dir(".") {
      dir("src") {
        dir("main") {
          dir("scala") {
            files("jaded.scala", "pom.xml")
          }
        }
      }
    }
  }

  describe("affected path composition") {
    it("should compose nicely") {
      val tree = (new Client).dirs
      tree match {
        case AffectedDirectory(".", _) => true
        case _ => fail()
      }
    }
  }

}
