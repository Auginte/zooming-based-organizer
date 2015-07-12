package com.auginte.scalajs.logic.view

import com.auginte.scalajs.logic.ElementPosition
import com.auginte.scalajs.state.T
import com.auginte.shared.state.persistable.{Camera, Persistable, Element, Position}
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
    selectedElement(state.persistable) match {
      case Some(element) => scaleWithElement(difference, element, state.camera)(state)
      case None => scaleView(difference, cursorPosition)(state)
    }
  }

  private def selectedElement(persistable: Persistable): Option[Element] =
    persistable.container.elements.find(_._2.selected == true).flatMap(pair => Some(pair._2))

  private def scaleView(difference: Double, cursorPosition: Position): T = { state =>
    changeScale(state.camera.scale, state.camera.scale * difference, cursorPosition)(state)
  }

  private def scaleWithElement(difference: Double, element: Element, camera: Camera): T = {
    val cursorPosition = ElementPosition(element, camera).center
    scaleView(difference, cursorPosition) andThen
      scaleElementReverse(element, difference, camera, cursorPosition)
  }

  private def scaleElementReverse(element: Element, difference: Double, camera: Camera, cursor: Position): T = { state =>
    val scaledBackElement = element.withScale(element.scale * difference)
    val elementBefore = ElementPosition(element, camera)
    val elementAfter = ElementPosition(scaledBackElement, camera)
    val positionDifferenceRendered = Position (
      (elementAfter.width - elementBefore.width) / 2.0,
      (elementAfter.height - elementBefore.height) / 2.0
    )
    val positionDifferenceInCamera = positionDifferenceRendered * camera.scale

    state inCamera {
      _ moved positionDifferenceInCamera
    } inContainer {
      _.elementUpdated(element, scaledBackElement)
    }
  }

  private def changeScale(oldScale: Double, newScale: Double, cursor: Position): T = { state =>
    val centerBefore = cursor * oldScale
    val centerAfter = cursor * newScale
    val positionDifference = centerBefore - centerAfter

    state inCamera {
      _ moved positionDifference and
        (_ withScale newScale)
    }
  }
}