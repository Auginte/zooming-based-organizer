package example

import example.communication.{Ajaj, Saved}
import example.events.Event._
import example.events._
import example.helpers.{DomLogger, Absolute, Proxy, Dom}
import example.logic.{elements, view}
import example.state._
import example.state.selected.Selectable
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

  private val defaultState = State(Camera(), Container(), Selectable(), Creation(), storage)

  val initialState = if (serialisedState.length == 0) defaultState else load(serialisedState) match {
    case Success(loadedState) =>
      loadedState.withStorage(storage)
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
        container withNewElement state.Element (
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
        case Some(element) => elements.Dragging.begin(element, e)
        case None => view.Dragging.begin(e)
      }
    }

    def drag(e: PointerEvent): Unit = preventDefault(e) {
      view.Dragging.drag(e) andThen elements.Dragging.drag(e)
    }

    def endDrag(e: PointerEvent) = preventDefault(e) {
      view.Dragging.end(e) compose elements.Dragging.end(e)
    }

    //
    // Zooming in desktop
    //

    def wheel(e: ReactWheelEvent): Unit = {
      val coefficient = 100
      val difference = 1 + (e.nativeEvent.deltaY / coefficient)
      val elementsAreaPos = Absolute.getAbsolute(Dom.findView(e.target))
      preventDefault(e) (view.Zooming.updateScale(difference, Position(e.clientX, e.clientY) - elementsAreaPos))
    }

    //
    // Dragging and zooming in mobile
    //

    def touch(reactEvent: ReactMouseEvent): Unit = event(reactEvent) match {
      case e: TouchStart if e.touchEvent.touches.length == 1 => selectedElement(reactEvent) match {
        case Some(element) => preventDefault(reactEvent) {
          elements.Dragging.begin(element, e.touchEvent.touches(0))
        }
        case None if isView(reactEvent.target) && e.touchEvent.touches.length == 1 => preventDefault(reactEvent) {
          view.Dragging.begin(e.touchEvent.changedTouches(0))
        }
        case None => Unit
      }
      case e: TouchStart if e.touchEvent.touches.length >= 2 => preventDefault(reactEvent) { state =>
        val pointer1 = viewPosition(e.touchEvent.touches(0), reactEvent.target)
        val pointer2 = viewPosition(e.touchEvent.touches(1), reactEvent.target)
        view.Zooming.initDistance(pointer1, pointer2)(state) and elements.Dragging.cancel
      }
      case e: TouchMove if e.touchEvent.touches.length >= 2 => preventDefault(reactEvent) {
        view.Zooming.updateDistance(
          viewPosition(e.touchEvent.changedTouches(0), reactEvent.target),
          viewPosition(e.touchEvent.changedTouches(1), reactEvent.target)
        ) andThen view.Dragging.cancel
      }
      case e: TouchEnd => preventDefault(reactEvent) {
         view.Zooming.saveZoom andThen
          view.Dragging.end(e.touchEvent.changedTouches(0)) andThen
           elements.Dragging.end(e.touchEvent.changedTouches(0))
      }
      case e: TouchCancel => preventDefault(reactEvent) {
        view.Zooming.cancel andThen
          view.Dragging.cancel andThen
          elements.Dragging.cancel
      }
      case otherEvents => Unit
    }

    //
    // Saving
    //

    def save()(implicit state: State = $.state): Unit = persist(state) {
      case (res, Success(saved)) if saved.success => redirect(saved.storage)
      case (res, Failure(error)) => log(s"Not saved: ${error.getMessage}. Response: ${res.responseText}")
    }

    //
    // Utilties
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

  private def load(jsonData: String) = Unpickle[State].fromString(jsonData)

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