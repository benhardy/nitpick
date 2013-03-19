package net.bhardy.nitpick.util

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import java.io.{InputStream, ByteArrayInputStream, File}

import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.Matchers.anyObject
/**
 * TODO add mockito
 */
class EnvironmentPropertiesSpec extends FunSpec with MustMatchers with MockitoSugar {

  /**
   * For testing purposes, construct an EnvironmentProperties
   * from an input string that looks like the contents of a properties
   * file.
   */
  def envFromProps(fakeContent: String): EnvironmentProperties = {
    val file: File = null // should never be used in test
    val fakeStream = new ByteArrayInputStream(fakeContent.getBytes)
    def fakeStreamOpener(file: File): InputStream = fakeStream
    EnvironmentProperties(file, fakeStreamOpener)
  }

  describe("EnvironmentProperties") {
    it("should create from a stream") {
      val env = envFromProps("some.property=someValue")
      env("some.property") must be === Some("someValue")
      env("unknown.property") must be === None
    }
  }

  describe("EnvironmentConfig") {

    it("should assemble from a correct set of EnvironmentProperties") {
      val fakeContent = "review.checkout.directory=/mnt/data/checkouts"
      val env = envFromProps(fakeContent)
      val config = new EnvironmentConfig(env)
      config.reviewCheckoutDirectory must be === "/mnt/data/checkouts"
    }

    it("should default to a /tmp for review.checkout.directory if that's missing") {
      val env = envFromProps("")
      val config = new EnvironmentConfig(env)
      config.reviewCheckoutDirectory must be === "/tmp"
    }
  }

  describe("SystemProperties") {
    it("runtimePropsPath should die if runtime.properties.file isn't defined") {
      val fakeSystemProperties = ""
      val env = envFromProps(fakeSystemProperties)
      val sp = new SystemProperties(env)
      intercept[IllegalStateException] {
        val p:File = sp.runtimePropsPath
      }
    }
    it("runtimePropsPath should proceed if runtime.properties.file is defined") {
      val fakeSystemProperties = "runtime.properties.file=/srv/thingy/go.properties"
      val env = envFromProps(fakeSystemProperties)
      val sp = new SystemProperties(env)
      val path = sp.runtimePropsPath
      path must be === (new File("/srv/thingy/go.properties"))
    }
  }

}

