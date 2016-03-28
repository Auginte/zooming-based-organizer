package com.auginte.scalajs.events

import org.scalajs.dom._

import scala.scalajs.js

/**
 * @see [[japgolly.scalajs.react.ReactTouchEvent]]
 */
@js.native
trait SyntheticTouchEvent extends js.Object {
  val touches: js.Array[Touch with ScreenPosition with ClientPosition] = js.native

  val changedTouches: js.Array[Touch with ScreenPosition with ClientPosition] = js.native

  val targetTouches: js.Array[Touch with ScreenPosition with ClientPosition] = js.native

  val altKey: Boolean = js.native

  val ctrlKey: Boolean = js.native

  val metaKey: Boolean = js.native

  val shiftKey: Boolean = js.native

  def getModifierState(key: String): Boolean = js.native
}
