package com.auginte.desktop

import scalafx.scene.Node
import scalafx.scene.input.MouseEvent
import javafx.scene.{input => jfxi}
import scalafx.event.Event
import javafx.{event => jfxe}
import javafx.event.EventHandler

/**
 * Improved ScalaFx Node for faster development.
 *
 * Mostly Closure based Event handling.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait RichNode extends Node {
  val mouseEvents = new Events[MouseEvent, jfxi.MouseEvent](e => new MouseEvent(e), h => onMouseClicked = h)

  /**
   * Class to store multiple event listeners.
   *
   * Example:
   * {{{
   *   val mouseEvents = new Events[MouseEvent, jfxi.MouseEvent](e => new MouseEvent(e), h => onMouseClicked = h)
   *   mouseEvents += ((e: MouseEvent) => println("Hello"))
   *   mouseEvents += ((e: MouseEvent) => println(e.x, e.y))
   * }}}
   *
   * @param converter function to convert ScalaFx event to JavaFx event. E.g. {{{e => new MouseEvent(e)}}}
   * @param observer function to update actual event Handler. E.g. {{{h => onMouseClicked = h}}}
   * @tparam S ScalaFx event type
   * @tparam J JavaFx event type
   */
  class Events[S <: Event, J <: jfxe.Event](converter: J => S, observer: EventHandler[_ >: J] => Unit) {
    private var events: List[S => Any] = List()
    private val handler = (e: S) => for (f <- events) f(e)

    def +=(f: S => Any) = {
      events = f :: events
      updateHandler()
    }

    def -=(f: S => Any) = {
      events = events diff List(f)
      updateHandler()
    }

    private def updateHandler() = {
      val eventHandler = delegateEvent(handler, converter)
      observer(eventHandler)
      eventHandler
    }

    private def delegateEvent[S <: Event, J <: jfxe.Event](f: S => Any, c: J => S): EventHandler[_ >: J] =
      new EventHandler[J] {
        def handle(event: J) = f(c(event))
      }
  }

}
