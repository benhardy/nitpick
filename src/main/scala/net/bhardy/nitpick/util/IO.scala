package net.bhardy.nitpick.util

import java.io.Closeable

/**
 * Fake automatic resource management
 * TODO drop if jsuereth's scala-arm library for this instead
 */
object IO {

  /**
   * Make sure blocks of code using a closeable resource actually get closed
   * no matter what.
   * @param resource - the item to be closed
   * @param f - a block of code to process the item return some value
   * @tparam C - the type of the closeable resource
   * @tparam R - the type of the return value
   * @return whatever gets returned from the passed block "f"
   * @throws anything that fBlock might throw
   */
  def using[C <: Closeable, R](resource: C)(f: C => R): R = {
    try {
      f(resource)
    }
    finally {
      closeQuietly(resource)
    }
  }

  /** for use in finally blocks only */
  def closeQuietly(closeable: Closeable) {
    if (closeable != null) {
      try {
        closeable.close()
      } catch {
        case _: Exception => // la la la
      }
    }
  }

}
