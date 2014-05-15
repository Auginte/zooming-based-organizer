package com.auginte.desktop.rich

import javafx.beans.property.ObjectProperty
import javafx.event.EventHandler
import scalafx.{event => sfxe}
import javafx.{event => jfxe}

/**
 * Event delegation.
 *
 * Takes ScalaFx event in closure form and adds it to JavaFx events.
 * Supports multiple events on one property. No event cancellation.
 *
 * {{{
 *   import scalafx.scene.{input => sfxi}
 *   import javafx.scene.{control = jfxc}
 *   class MyTextArea extends jfxc.TextArea with RichNode[jfxc.TextArea] {}
 *   val t = new MyTextArea
 *   t.onMouseClicked += ((e: sfxi.MouseEvent) => println("Clicked: " + e.x + "x" e.y))
 *   t.onMouseClicked += ((e: sfxi.MouseEvent) => println("With button: " + e.button))
 * }}}
 *
 * @param delegate property of JavFx node
 * @param java2Scala function to create ScalaFx event object
 * @tparam S ScalaFx event
 * @tparam J JavaFx event
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Event[S <: sfxe.Event, J <: jfxe.Event](delegate: => ObjectProperty[EventHandler[_ >: J]], java2Scala: J => S) {
  type EventsList = List[J => Any]
  var events: EventsList = List[J => Any]()

  if (delegate.get() != null) {
    events = ((e: J) => delegate.get().handle(e)) :: events
  }

  private val eventHandler: EventHandler[_ >: J] = new EventHandler[J] {
    def handle(e: J): Unit = for (handler <- events) handler(e)
  }
  delegate.set(eventHandler)

  //  private def delegateEvent[S <: Event, J <: jfxe.Event](f: S => Any, c: J => S): EventHandler[_ >: J] =
  //    new EventHandler[J] {
  //      def handle(event: J) = f(c(event))
  //    }

  def +=(f: S => Any): EventsList = {
    events = convert(f) :: events
    events
  }

  def -=(f: S => Any): EventsList = {
    events = events diff List(convert(f))
    events
  }

  def replace(f: S => Any): EventsList = {
    events = List(convert(f))
    events
  }

  @inline
  private def convert(f: S => Any): J => Any = (e: J) => f(java2Scala(e))
}
