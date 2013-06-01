package net.bhardy.nitpick.config

import java.io.File

import net.bhardy.nitpick.NitpickServlet
import net.bhardy.nitpick.service.{ReviewService, ReviewServiceImpl}
import net.bhardy.nitpick.util.{SystemProperties, EnvironmentConfig, EnvironmentProperties}

/**
 * Wires dependencies together.
 */
object RuntimeConfig {

  val runtimePropertiesPath = SystemProperties.runtimePropsPath
  val runtimeProperties = runtimePropertiesPath.map{ (path:File) => EnvironmentProperties.load(path) } 
      .getOrElse(EnvironmentProperties.defaults)

  implicit val envConfig = new EnvironmentConfig(runtimeProperties)
  implicit val reviewService: ReviewService = new ReviewServiceImpl
  implicit val nitpickServlet = new NitpickServlet
}




