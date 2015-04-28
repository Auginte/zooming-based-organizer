package com.auginte.scalajs.events

import scala.scalajs.js

/**
 * JavaScript object, which have client position
 */
trait ClientPosition extends js.Object {
  val clientX: Double = js.native

  val clientY: Double = js.native
}