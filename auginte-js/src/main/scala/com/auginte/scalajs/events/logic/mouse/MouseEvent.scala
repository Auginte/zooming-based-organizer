package com.auginte.scalajs.events.logic.mouse

import com.auginte.scalajs.events.PointerEvent
import com.auginte.scalajs.events.logic.Event

trait MouseEvent extends Event {
  val reactMouseEvent: PointerEvent
}
