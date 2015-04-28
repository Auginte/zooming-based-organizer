package com.auginte.scalajs

import com.auginte.scalajs.communication.Ajaj
import com.auginte.scalajs.events.{event, PointerEvent, ClientPosition, Event}
import com.auginte.scalajs.events.Event.{TouchCancel, TouchEnd, TouchMove, TouchStart}
import com.auginte.scalajs.helpers.{Dom, Proxy, Absolute, DomLogger}
import com.auginte.scalajs.logic.elements.Dragging
import com.auginte.scalajs.logic.view.Zooming
import com.auginte.scalajs.state.State
import com.auginte.scalajs.state.persistable._
import com.auginte.scalajs.state.selected.Selectable
import com.auginte.scalajs.state._
import Event._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.Attr
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import scala.language.postfixOps
import scala.scalajs.js
import js.Dynamic.{global => g}
import prickle._
import scala.util.{Failure, Success}

/**
 * Plane with functions to add, zoom and drag elements and plane itself.
 * 
 * @param serialisedState JSON picled State object
 * @param storage meta data for relation with persistance storage
 */
class DragableElements(serialisedState: String = "", storage: Storage = Storage()) extends DomLogger with Ajaj {

  //
  // Loading
  //

  private val defaultState = State(Persistable(Camera(), Container(), Selectable(), Storage()), Creation())

  val initialState: State = if (serialisedState.length == 0) defaultState else load(serialisedState) match {
    case Success(loadedState) =>
      State(loadedState.withStorage(storage), defaultState.creation)
    case Failure(error) =>
      log(s"Not loaded: ${error.getMessage}. Input: $serialisedState")
      defaultState
  }

  //
  // High level logic (Interacting with React events)
  //
  sealed class Backend(val $: BackendScope[Unit, State]) extends helpers.Backend[State] {
    val initialTop = 40
    val initialHeight = 20
    val initialWidth = 50

    //
    // Adding new elements
    //

    def saveName(e: ReactEventI) = m(_ inCreation(_ withName e.target.value))

    def addElement(e: ReactEvent) = preventDefault(e) { currentState =>
      currentState inCreation (_ resetName) inContainer { container =>
        container withNewElement persistable.Element (
          container.nextId,
          currentState.creation.name,
          0,
          initialTop + initialHeight * container.elements.size,
          initialWidth,
          initialHeight
        )
      }
    }

    //
    // Dragging elements
    //

    def beginDrag(e: PointerEvent): Unit = preventDefault(e) {
      selectedElement(e) match {
        case Some(element) => Dragging.begin(element, e)
        case None => logic.view.Dragging.begin(e)
      }
    }

    def drag(e: PointerEvent): Unit = preventDefault(e) {
      logic.view.Dragging.drag(e) andThen logic.elements.Dragging.drag(e)
    }

    def endDrag(e: PointerEvent) = preventDefault(e) {
      logic.view.Dragging.end(e) compose logic.elements.Dragging.end(e)
    }

    //
    // Zooming in desktop
    //

    def wheel(e: ReactWheelEvent): Unit = {
      val coefficient = 100
      val difference = 1 + (e.nativeEvent.deltaY / coefficient)
      val elementsAreaPos = Absolute.getAbsolute(Dom.findView(e.target))
      preventDefault(e) (Zooming.updateScale(difference, Position(e.clientX, e.clientY) - elementsAreaPos))
    }

    //
    // Dragging and zooming in mobile
    //

    def touch(reactEvent: ReactMouseEvent): Unit = event(reactEvent) match {
      case e: TouchStart if e.touchEvent.touches.length == 1 => selectedElement(reactEvent) match {
        case Some(element) => preventDefault(reactEvent) {
          logic.elements.Dragging.begin(element, e.touchEvent.touches(0))
        }
        case None if isView(reactEvent.target) && e.touchEvent.touches.length == 1 => preventDefault(reactEvent) {
          logic.view.Dragging.begin(e.touchEvent.changedTouches(0))
        }
        case None => Unit
      }
      case e: TouchStart if e.touchEvent.touches.length >= 2 => preventDefault(reactEvent) { state =>
        val pointer1 = viewPosition(e.touchEvent.touches(0), reactEvent.target)
        val pointer2 = viewPosition(e.touchEvent.touches(1), reactEvent.target)
        logic.view.Zooming.initDistance(pointer1, pointer2)(state) and logic.elements.Dragging.cancel
      }
      case e: TouchMove if e.touchEvent.touches.length >= 2 => preventDefault(reactEvent) {
        logic.view.Zooming.updateDistance(
          viewPosition(e.touchEvent.changedTouches(0), reactEvent.target),
          viewPosition(e.touchEvent.changedTouches(1), reactEvent.target)
        ) andThen logic.view.Dragging.cancel
      }
      case e: TouchEnd => preventDefault(reactEvent) {
         logic.view.Zooming.saveZoom andThen
          logic.view.Dragging.end(e.touchEvent.changedTouches(0)) andThen
           logic.elements.Dragging.end(e.touchEvent.changedTouches(0))
      }
      case e: TouchCancel => preventDefault(reactEvent) {
        logic.view.Zooming.cancel andThen
          logic.view.Dragging.cancel andThen
          logic.elements.Dragging.cancel
      }
      case otherEvents => Unit
    }

    //
    // Saving
    //

    def save()(implicit state: Persistable = $.state.persistable): Unit = persist(state) {
      case (res, Success(saved)) if saved.success => redirect(saved.storage)
      case (res, Failure(error)) => log(s"Not saved: ${error.getMessage}. Response: ${res.responseText}")
    }

    //
    // Utilities
    //

    private def selectedElement(e: ReactMouseEvent): Option[Element] = {
      val elements = $.state.container.elements
      Dom.getData(e.target, "element-id") map(_.toInt) match {
        case Some(id: Id)  if elements.contains(id) => Some(elements(id))
        case _ => None
      }
    }

    private def isView(node: dom.Node) = {
      val className = Dom.attribute(node, "class", "")
      if (className != "") className == "area" else false
    }

    def viewPosition(clientPosition: ClientPosition, target: dom.Node): Position = {
      val elementsAreaPos = Absolute.getAbsolute(Dom.findView(target))
      Position(clientPosition.clientX, clientPosition.clientY) - elementsAreaPos
    }
  }

  private def load(jsonData: String) = Unpickle[Persistable].fromString(jsonData)

  /**
   * Passing events to Backend
   */
  case class EventsProxy(element: Element, camera: Camera, B: Backend) extends Proxy[PointerEvent, Element, Camera] {
    override def receive(reactEvent: PointerEvent)(implicit sender: Element): Unit = event(reactEvent) match {
      case MouseDown(e) => B.beginDrag(reactEvent)
      case e: TouchStart if e.touchEvent.touches.length > 0 => B.touch(reactEvent)
      case e: TouchEnd if e.touchEvent.touches.length > 0 => B.touch(reactEvent)
      case e: TouchCancel if e.touchEvent.touches.length > 0 => B.touch(reactEvent)
      case other => Unit
    }
  }

  //
  // Front end: HTML events passing to backend
  //
  val render = ReactComponentB[Unit]("Page")
    .initialState(initialState)
    .backend(new Backend(_))
    .render { (P, S, B) =>

    <.div(
      <.form(
        ^.onSubmit ==> B.addElement,
        <.input(
          ^.onChange ==> B.saveName,
          ^.value := S.creation.name,
          ^.`class` := "new-name"
        ),
        <.button("Add"),
        <.button(
          "Save",
          ^.onClick --> B.save,
          ^.`type` := "button"
        )
      ),
      <.div(
        S.container.elements map {
          case (id, element) => Element.render.withKey(id)(EventsProxy(element, S.camera, B))
        }
      ),
      ^.onMouseDown ==> B.beginDrag,
      ^.onMouseUp ==> B.endDrag,
      ^.onMouseMove ==> B.drag,
      Attr("onWheel") ==> B.wheel,
      ^.onTouchStart ==> B.touch,
      ^.onTouchMove ==> B.touch,
      ^.onTouchEnd ==> B.touch,
      ^.onTouchCancel ==> B.touch,
      ^.`class` := "area"
    )
  }
  .buildU
}