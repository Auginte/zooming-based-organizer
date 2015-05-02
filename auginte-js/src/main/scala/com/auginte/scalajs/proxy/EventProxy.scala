package com.auginte.scalajs.proxy

import com.auginte.scalajs.events.logic
import japgolly.scalajs.react.{ReactEventI, ReactEvent}

trait EventProxy {
  def receive(event: logic.Event)(context: ReactEvent): Unit
}
