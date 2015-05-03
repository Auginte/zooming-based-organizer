package com.auginte.scalajs

import com.auginte.scalajs.communication.Ajaj
import com.auginte.scalajs.events._
import com.auginte.scalajs.events.logic.mouse.Wheel
import com.auginte.scalajs.events.logic.touch
import com.auginte.scalajs.helpers._
import com.auginte.scalajs.logic.elements.Dragging
import com.auginte.scalajs.logic.view.Zooming
import com.auginte.scalajs.proxy._
import com.auginte.scalajs.state.State
import com.auginte.scalajs.state.persistable._
import com.auginte.scalajs.state.selected.Selectable
import com.auginte.scalajs.state._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom
import scala.language.postfixOps
import scala.scalajs.js
import js.Dynamic.{global => g}
import prickle._
import scala.util.{Failure, Success}
import com.auginte.scalajs.events.logic.{Event => LogicEvent, _}
import com.auginte.scalajs.events.SyntheticTouchEvent


/**
 * Plane with functions to add, zoom and drag elements and plane itself.
 * 
 * @param serialisedState JSON picled State object
 * @param storage meta data for relation with persistance storage
 */
class Page(serialisedState: String = "", storage: Storage = Storage()) extends DomLogger with Ajaj {

  //
  // Loading
  //

  private val defaultState = State(Persistable(Camera(), Container(), Selectable(), storage), state.Creation(), Menu())

  val initialState: State = if (serialisedState.length == 0) defaultState else load(serialisedState) match {
    case Success(loadedState) =>
      State(loadedState.withStorage(storage), defaultState.creation, defaultState.menu)
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

    def creationProxy() = new CreationProxy($.state.creation) {
      override def receive(event: LogicEvent)(context: ReactEvent): Unit = event match {
        case e: Add => addElement(context)
        case SaveName(name) => saveName(name)
        case _ =>
      }

      private def addElement(e: ReactEvent) = preventDefault(e) { currentState =>
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

      private def saveName(name: String) = m(_ inCreation(_ withName name))
    }

    def viewProxy = new ViewProxy($.state.persistable) {

      //
      // Dragging
      //

      val mouseDragging: Actions = {
        case ElementEvent(mouse.Drag(e), _, _) =>  dragActive(e)
        case mouse.DragBegin(e) => selectedElement(e) match {
          case Some(element) => Dragging.begin(element, e)
          case None => logic.view.Dragging.begin(e)
        }
        case mouse.Drag(e) => dragActive(e)
        case mouse.DragEnd(e) => endDragActive(e)
      }

      val touchDragging: Actions = {
        case touch.DragBegin(e) if e.touches.length == 1 =>
          logic.view.Dragging.begin(synthetic(e).changedTouches(0))
        case ElementEvent(touch.DragBegin(e), element, _) if e.touches.length == 1 =>
          logic.elements.Dragging.begin(element, synthetic(e).touches(0))
      }

      private def dragActive(e: PointerEvent): T =
        logic.view.Dragging.drag(e) andThen logic.elements.Dragging.drag(e)

      private def endDragActive(e: PointerEvent): T =
        logic.view.Dragging.end(e) compose logic.elements.Dragging.end(e)

      private def synthetic(event: ReactTouchEvent) = event.asInstanceOf[SyntheticTouchEvent]

      private def draggingView = $.state.selected.elements.id.isEmpty


      //
      // Zooming
      //

      val zoomingActions: Actions = {
        case Wheel(e) =>
          val coefficient = 100
          val difference = 1 + (e.nativeEvent.deltaY / coefficient)
          val elementsAreaPos = Absolute.getAbsolute(Dom.findView(e.target))
          Zooming.updateScale(difference, Position(e.clientX, e.clientY) - elementsAreaPos)
        case touch.DragBegin(e) if e.touches.length == 2 =>
          val event = synthetic(e)
          val pointer1 = viewPosition(event.touches(0), e.target)
          val pointer2 = viewPosition(event.touches(1), e.target)
          logic.view.Zooming.initDistance(pointer1, pointer2) andThen logic.elements.Dragging.cancel
        case touch.Drag(e) if e.touches.length >= 2 =>
          val event = synthetic(e)
          logic.view.Zooming.updateDistance(
            viewPosition(event.changedTouches(0), e.target),
            viewPosition(event.changedTouches(1), e.target)
          ) andThen logic.view.Dragging.cancel

      }

      //
      // Finishing zooming and dragging
      //

      val finishActions: Actions = {
        case touch.DragEnd(e) =>
          logic.view.Zooming.saveZoom andThen
            logic.view.Dragging.end(synthetic(e).changedTouches(0)) andThen
            logic.elements.Dragging.end(synthetic(e).changedTouches(0))
        case touch.DragCancel(_) =>
          logic.view.Zooming.cancel andThen
            logic.view.Dragging.cancel andThen
            logic.elements.Dragging.cancel
      }

      //
      // Combined
      //

      override val actions: Actions = combine(mouseDragging, touchDragging, zoomingActions, finishActions)

      override protected def save(transformation: T): Unit = m(transformation)
    }

    //
    // Sidebar
    //

    def sidebarProxy() = new SidebarProxy($.state.menu) {
      override def receive(event: LogicEvent)(context: ReactEvent): Unit = event match {
        case e: Save => save()
        case e: ToggleSidebar => preventDefault(context)(_.inMenu(_.toggleExpanded))
        case _ =>
      }
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

  //
  // Front end: HTML events passing to backend
  //

  val render = ReactComponentB[Unit]("Page")
    .initialState(initialState)
    .backend(new Backend(_))
    .render { (P, S, B) =>

    <.div(
      Sidebar.generate(B.sidebarProxy()),
      Creation.generate(B.creationProxy()),
      View.generate(B.viewProxy)
    )
  }
  .buildU
}