package example.logic.view

import example.events.ScreenPosition
import example.state._

import scala.language.postfixOps

/**
 * Changing state to implement dragging of the Plane/view
 */
object Dragging {
  def begin(e: ScreenPosition): T = select andThen saveDrag(e)

  def drag(e: ScreenPosition): T = { state =>
    if (isSelected(state)) moveCamera(e)(state) and saveDrag(e)
    else state
  }

  def end(e: ScreenPosition): T = { state =>
    if (isSelected(state)) deselect(state) and moveCamera(e)
    else state
  }

  def cancel: T = _ inSelected (_ inCamera(_ deselect))

  private def isSelected(state: State) = state.selected.camera.isSelected

  private def select: T = _ inSelected (_ inCamera(_ select))

  private def deselect: T = _ inSelected (_ inCamera (_ deselect))

  private def saveDrag(e: ScreenPosition): T = { state =>
    state inSelected (_ inCamera (_ inMovable(_ withPosition inCamera(state, e))))
  }

  private def moveCamera(e: ScreenPosition): T = { state =>
    val difference = state.selected.camera.movable.difference(inCamera(state, e))
    state inCamera (_ moved difference)
  }

  private def inCamera(state: State, position: ScreenPosition): Position =
    inCameraCoordinates(state.camera)(Position(position.screenX, position.screenY))

  private def inCameraCoordinates(camera: Camera): Tr[Position] =  -_ * camera.scale
}