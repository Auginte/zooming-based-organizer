package com.auginte.scalajs.events

import scala.scalajs.js

/**
 * JavaScript object, that have prevent action functionality
 */
trait EventFlow extends js.Object {
  def preventDefault(): Unit = js.native
}