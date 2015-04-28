package com.auginte.scalajs

import japgolly.scalajs.react._

package object events {
  /**
   * Common functionality of MouseEvent and Touch in TouchEvent
   */
  type PointerEvent = ReactMouseEvent with ScreenPosition with ClientPosition with EventFlow

  import Event._

  def event(event: ReactEvent): Event = event.nativeEvent.`type` match {
    case "mousedown" => MouseDown(event.asInstanceOf[PointerEvent])
    case "mousemove" => MouseMove(event.asInstanceOf[PointerEvent])
    case "mouseup"  => MouseUp(event.asInstanceOf[PointerEvent])
    case "touchstart" => TouchStart(event)
    case "touchmove" => TouchMove(event)
    case "touchend" => TouchEnd(event)
    case _ => new MouseEvent(event.asInstanceOf[PointerEvent])
  }
}
