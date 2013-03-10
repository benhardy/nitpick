package net.bhardy.nitpick.util

import java.io._
import net.bhardy.nitpick.util.IO.using
import scala.util.Try

/**
 * Keep track of an incrementing counter in a file
 */
class CounterFile(path:File) {

  /**
   * get the current counter value. If it cannot be read for
   * whatever reason, 0 will be returned.
   * @return
   */
  def current: Int = {
    val defaultValue = 0
    try {
      using(new FileReader(path)) { reader =>
        using(new BufferedReader(reader)) { bufReader =>
          bufReader.readLine().trim.toInt
        }
      }
    } catch {
      case e: NumberFormatException => defaultValue
      case io: IOException => defaultValue
    }
  }

  /**
   * @return next value in sequence.
   * @throws IOException if it can't write
   */
  def next: Int = {
    val nextVal = current + 1

    using(new FileWriter(path)) { writer =>
      using(new BufferedWriter(writer)) { bufWriter =>
        bufWriter.append(nextVal.toString)
      }
    }
    nextVal
  }
}
