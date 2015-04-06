package example.logic.view

import example.state._
import scala.language.postfixOps

/**
 * Changing state to implement Zooming of the plane/Camera
 */
object Zooming {
  def initDistance(pointer1: Position, pointer2: Position): T =
    _ inSelected (_ inCamera(_ inZoomable {
      _ withLastDistance(pointer1 distance pointer2) withLastPosition (pointer1 center pointer2)
    }))

  def updateDistance(pointer1: Position, pointer2: Position): T =
    _ inSelected (_ inCamera(_ inZoomable {
      _ withDistance(pointer1 distance pointer2)
    }))

  def saveZoom: T = { state =>
    if (!state.selected.camera.zoomable.isZooming) state
    else {
      val oldDistance = state.selected.camera.zoomable.lastDistance
      val newDistance = state.selected.camera.zoomable.currentDistance
      val oldScale = state.camera.scale
      val newScale = (oldScale * oldDistance) / newDistance
      val cursor = state.selected.camera.zoomable.last

      changeScale(oldScale, newScale, cursor)(state) and cancel
    }
  }

  def cancel: T = _ inSelected(_ inCamera(_ inZoomable(_ cancelZooming)))

  def updateScale(difference: Double, cursorPosition: Position): T = { state =>
    changeScale(state.camera.scale, state.camera.scale * difference, cursorPosition)(state)
  }

  private def changeScale(oldScale: Double, newScale: Double, cursor: Position): T = {state =>
    val centerBefore = cursor * oldScale
    val centerAfter = cursor * newScale
    val postionDifference = centerBefore - centerAfter

    state inCamera {
      _ moved postionDifference and
        (_ withScale newScale)
    }
  }
}