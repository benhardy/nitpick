package net.bhardy.nitpick.util

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import java.io.{InputStream, ByteArrayInputStream, File}
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.Matchers.anyObject
import java.util.Properties

import net.bhardy.nitpick.util.IO.using

/**
 * TODO add mockito
 */
class EnvironmentPropertiesSpec extends FunSpec with MustMatchers with MockitoSugar {


  describe("EnvironmentProperties") {
    it("should load from a path if given one") {
      val props = EnvironmentProperties.load(new File("src/test/resources/runtime.properties"))
      props("review.checkout.directory") must be === Some("/var/tmp/nitpick")
    }
    it("defaults should be empty") {
      val props = EnvironmentProperties.defaults
      props("review.checkout.directory") must be === None
    }
  }

  describe("EnvironmentConfig") {

    it("should assemble from a correct set of EnvironmentProperties") {
      def fakeContent = (k:String) => if (k equals "review.checkout.directory") Some("/mnt/data/checkouts") else None

      val config = new EnvironmentConfig(EnvironmentProperties from fakeContent)
      config.reviewCheckoutDirectory must be === "/mnt/data/checkouts"
    }

    it("should default to a /tmp for review.checkout.directory if that's missing") {
      def fakeContent = (k:String) => if (k equals "impossible") Some("garbage") else None
      val config = new EnvironmentConfig(EnvironmentProperties from fakeContent)
      config.reviewCheckoutDirectory must be === "/tmp"
    }
  }

  describe("SystemProperties") {
    it("runtimePropsPath should return None if runtime.properties.file isn't defined") {
      def fakeContent = (k:String) => if (k equals "nothing") Some("nothing") else None

      val env = EnvironmentProperties from fakeContent
      val sp = new SystemProperties(env)
      val p:Option[File] = sp.runtimePropsPath
      p must be === None
    }
    it("runtimePropsPath should proceed if runtime.properties.file is defined") {
      def fakeContent = (k:String) => if (k equals "runtime.properties.file") Some("/srv/thingy/go.properties") else None

      val env = EnvironmentProperties from fakeContent
      val sp = new SystemProperties(env)
      val path = sp.runtimePropsPath
      path must be === Some(new File("/srv/thingy/go.properties"))
    }
  }

}

