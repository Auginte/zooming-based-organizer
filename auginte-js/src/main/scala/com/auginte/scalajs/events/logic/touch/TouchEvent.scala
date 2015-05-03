package com.auginte.scalajs.events.logic.touch

import com.auginte.scalajs.events.logic.Event
import japgolly.scalajs.react.ReactTouchEvent

trait TouchEvent extends Event {
  val reactTouchEvent: ReactTouchEvent
}
