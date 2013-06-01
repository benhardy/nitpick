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

  /**
   * Load the EnvironmentProperties from the specified file.
   * By default we just want to open the file in
   * the regular way. We do something else in tests.
   */
  def load(path :File): EnvironmentProperties = {
    val propertiesFromFile = propertyLoad(path)
    from(key => Option(propertiesFromFile.getProperty(key)))
  }

  def propertyLoad(path: File): Properties = {
    val p = new Properties
    p.load(new FileInputStream(path))
    p
  }

  /**
   * Wrap a String=>Option[String] function in an EnvironmentProperties
   */
  def from(func: String => Option[String]) = new EnvironmentProperties {
    override def apply(key: String) = func(key)
  }

  def emptyProperties(key:String): Option[String] = None

  val defaults = from(emptyProperties)
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
  def runtimePropsPath: Option[File] = {
    props(runtimeConfigFileProperty).map { path => new File(path) }
  }
}

object SystemProperties extends SystemProperties
