package com.auginte.scalajs.events

import scala.scalajs.js

/**
 * JavaScript object, which have screen position
 */
@js.native
trait ScreenPosition extends js.Object {
  val screenX: Double = js.native

  val screenY: Double = js.native
}