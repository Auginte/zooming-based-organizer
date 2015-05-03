package com.auginte.scalajs.events.logic.mouse

import com.auginte.scalajs.events.logic.Event
import japgolly.scalajs.react.ReactWheelEvent

/**
 * using mouse wheel
 */
case class Wheel(reactWheelEvent: ReactWheelEvent) extends Event
