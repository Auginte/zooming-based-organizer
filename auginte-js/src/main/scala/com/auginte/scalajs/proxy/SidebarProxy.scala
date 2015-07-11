package com.auginte.scalajs.proxy

import com.auginte.scalajs.events.logic.Event
import com.auginte.scalajs.state.Menu
import com.auginte.shared.state.persistable.Persistable
import japgolly.scalajs.react.ReactEvent

abstract class SidebarProxy(val state: Menu) extends EventProxy {
  protected def persistableState: Persistable

  private def delegateReceived(event: Event)(reactEvent: ReactEvent) = receive(event)(reactEvent)

  def generateSelectedOperations = new SelectedOperationsProxy(persistableState) {
    override def receive(event: Event)(reactEvent: ReactEvent): Unit = delegateReceived(event)(reactEvent)
  }
}
