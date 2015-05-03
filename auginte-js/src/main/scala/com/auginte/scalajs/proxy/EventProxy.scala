package com.auginte.scalajs.proxy

import com.auginte.scalajs.events._
import japgolly.scalajs.react._

trait EventProxy {
  def receive(event: logic.Event)(reactEvent: ReactEvent): Unit

  final def inputReceive(converter: ReactEventI => logic.Event)(reactEvent: ReactEventI) =
    receiveType(converter, reactEvent)

  final def touchReceive(converter: ReactTouchEvent => logic.Event)(reactEvent: ReactTouchEvent) =
    receiveType(converter, reactEvent)

  final def wheelReceive(converter: ReactWheelEvent => logic.Event)(reactEvent: ReactWheelEvent) =
    receiveType(converter, reactEvent)

  final def mouseReceive(converter: PointerEvent => logic.Event)(reactEvent: PointerEvent) =
    receiveType(converter, reactEvent)

  private def receiveType[E <: ReactEvent](converter: E => logic.Event, reactEvent: E) = {
    val event = converter(reactEvent)
    receive(event)(reactEvent)
  }
}
