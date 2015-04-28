package com.auginte.scalajs.events

import japgolly.scalajs.react._

import scala.scalajs.js

/**
 * Helper for JavaScript/React event conversion to object
 */
abstract sealed class Event(val _reactEvent: ReactEvent) {
  val dynamic = _reactEvent.asInstanceOf[js.Dynamic]
}
object Event {
  def unapply(original: Event) = Some(original._reactEvent)

  sealed class MouseEvent(reactEvent: PointerEvent) extends Event(reactEvent)
  case class MouseDown(reactEvent: PointerEvent) extends MouseEvent(reactEvent)
  case class MouseMove(reactEvent: PointerEvent) extends MouseEvent(reactEvent)
  case class MouseUp(reactEvent: PointerEvent) extends MouseEvent(reactEvent)
  sealed class TouchEvent(reactEvent: ReactEvent) extends Event(reactEvent) {
    val touchEvent = reactEvent.asInstanceOf[SyntheticTouchEvent]
  }
  case class TouchStart(reactEvent: ReactEvent) extends TouchEvent(reactEvent)
  case class TouchMove(reactEvent: ReactEvent) extends TouchEvent(reactEvent)
  case class TouchEnd(reactEvent: ReactEvent) extends TouchEvent(reactEvent)
  case class TouchCancel(reactEvent: ReactEvent) extends TouchEvent(reactEvent)
}