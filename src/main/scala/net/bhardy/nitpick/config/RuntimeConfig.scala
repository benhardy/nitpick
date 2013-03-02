package net.bhardy.nitpick.config

import net.bhardy.nitpick.NitpickServlet
import net.bhardy.nitpick.service.{ReviewService, ReviewServiceImpl}
import net.bhardy.nitpick.util.{SystemProperties, EnvironmentConfig, EnvironmentProperties}

/**
 * Wires dependencies together.
 */
object RuntimeConfig {

  val envProperties = EnvironmentProperties(SystemProperties.runtimePropsPath)

  implicit val envConfig = new EnvironmentConfig(envProperties)
  implicit val reviewService: ReviewService = new ReviewServiceImpl
  implicit val nitpickServlet = new NitpickServlet
}




