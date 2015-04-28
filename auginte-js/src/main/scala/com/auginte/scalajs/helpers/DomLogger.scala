package com.auginte.scalajs.helpers

import scala.scalajs.js.Dynamic.{global => g}

/**
 * Helper to log errors to console, if it exists in the browser
 */
trait DomLogger {
  protected def log(message: String): Unit = {
    if (!g.console.isInstanceOf[Unit]) {
      try {
        g.console.warn(message)
      } catch {
        case e:Exception => // Just ignore
      }
    }
  }
}
