package com.auginte.scalajs.proxy

import com.auginte.scalajs.events.logic
import japgolly.scalajs.react.ReactEvent

trait EventProxy {
  def receive(event: logic.Event)(context: ReactEvent): Unit
}
