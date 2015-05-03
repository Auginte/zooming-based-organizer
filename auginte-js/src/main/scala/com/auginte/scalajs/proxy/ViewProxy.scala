package com.auginte.scalajs.proxy

import com.auginte.scalajs.events.logic
import com.auginte.scalajs.events.logic.{ElementEvent, Event}
import com.auginte.scalajs.state.{Actions, T, State}
import com.auginte.scalajs.state.persistable.{Camera, Persistable}
import japgolly.scalajs.react.ReactEvent
import japgolly.scalajs.react.vdom.ReactTag

abstract class ViewProxy(val state: Persistable) extends EventProxy {
  def elements(generator: ElementProxy => ReactTag, camera: Camera) = {
    state.container.elements map {
      case (id, element) =>  generator(new ElementProxy(element, camera) {
        override def receive(event: Event)(context: ReactEvent): Unit =
          delegateReceived(ElementEvent(event, element,camera))(context)
      })
    }
  }

  private final def delegateReceived(event: logic.Event)(context: ReactEvent) = receive(event)(context)

  override final def receive(event: Event)(context: ReactEvent): Unit = if (actions.isDefinedAt(event)) {
    context.preventDefault()
    save(actions(event))
  }

  val actions: Actions

  protected def save(transformation: T): Unit
  
  protected final def combine(functions: Actions*) = functions.reduceLeft(_ orElse _)
}
