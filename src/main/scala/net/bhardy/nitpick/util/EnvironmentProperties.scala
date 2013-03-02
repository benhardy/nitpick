package net.bhardy.nitpick.util

import java.io.{InputStream, FileInputStream, File}
import java.util.Properties

/**
 * Container for properties which come from somewhere in the
 * environment.
 */
trait EnvironmentProperties extends (String => Option[String])

/**
 * loader can produce EnvironmentProperties from any File
 */
object EnvironmentProperties {

  def streamFromFile(file: File): InputStream = {
    new FileInputStream(file)
  }

  /**
   * Upgrade a String=>Option[String] func to EnvironmentProperties
   */
  def from(func: String => Option[String]) = new EnvironmentProperties {
    override def apply(key: String) = func(key)
  }

  /**
   * Load the EnvironmentProperties from the specified file.
   * By default we just want to open the file in
   * the regular way. We do something else in tests.
   */
  def apply(file:File, fileOpener: File=>InputStream = streamFromFile) = {
    val propertiesFromFile = new Properties
    propertiesFromFile.load(fileOpener(file))
    from(key => Option(propertiesFromFile.getProperty(key)))
  }
}

/**
 * environment-sensitive parameters
 */
class EnvironmentConfig private(
                         val reviewCheckoutDirectory: String
                         ) {
  def this(props: EnvironmentProperties) = {
    this(props("review.checkout.directory").getOrElse("/tmp"))
  }
}

class SystemProperties private[util](props:EnvironmentProperties) {
  def this() = this(
    EnvironmentProperties.from(key => Option(System.getProperty(key)))
  )

  val runtimeConfigFileProperty = "runtime.properties.file"

  /**
   * Figure out the path of the runtime properties file
   */
  def runtimePropsPath: File = {
    val path = props(runtimeConfigFileProperty).getOrElse {
      val error = "system property '" + runtimeConfigFileProperty +
        "' was not specified, giving up"
      throw new IllegalStateException(error)
    }
    new File(path)
  }
}

object SystemProperties extends SystemProperties