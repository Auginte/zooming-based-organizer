package com.auginte.scalajs.logic.elements

import com.auginte.scalajs.events.ScreenPosition
import com.auginte.scalajs.state._
import com.auginte.scalajs.state.persistable.{Position, Element, Camera}
import scala.language.postfixOps

/**
 * Changing state to implement Dragging of the element
 */
object Dragging {
  def begin(element: Element, event: ScreenPosition) = beginDrag(element.id, event)

  def end(event: ScreenPosition): T = { state: State => endDrag(event)(state) }

  def drag(event: ScreenPosition): T = moveElement(event) andThen savePosition(event)

  private def beginDrag(elementId: Id, position: ScreenPosition): T =
    savePosition(position)(_) inSelected (_ inElements (_ withSelected elementId))

  private def savePosition(position: ScreenPosition): T = { state =>
    state inSelected (_ inElements (_ withPosition inCamera(state, position)))
  }

  private def endDrag(position: ScreenPosition): T =
    moveElement(position)(_) inSelected (_ inElements (_ deselect))

  private def moveElement(position: ScreenPosition): T = { state => state inContainer { container =>
    val selected = state.selected
    val positionChange = selected.elements.difference(inCamera(state, position))
    container moved(selected.elements.id, positionChange)
  }
  }

  private def inCamera(state: State, position: ScreenPosition): Position =
    inCameraCoordinates(state.camera)(Position(position.screenX, position.screenY))

  private def inCameraCoordinates(camera: Camera): Tr[Position] =  _ * camera.scale

  def cancel: T = _ inSelected (_ inElements (_ deselect))
}